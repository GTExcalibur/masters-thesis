package gturner.solver.impl;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import gturner.crossword.spec.CWClue;
import gturner.crossword.spec.CWPuzzle;
import gturner.expert.IClueExpert;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created with IntelliJ IDEA.
 * User: George Turner
 * Date: 10/11/14
 * Time: 7:27 PM
 * To change this template use File | Settings | File Templates.
 */
public final class WeightAwarePuzzleSolver implements IPuzzleSolver {

    private final IClueExpert fillInBlankExpert;
    private final ExecutorService executorService;
    private final int maxDiscrepancyCount;
    private boolean bailOnEmpty;

    public WeightAwarePuzzleSolver(IClueExpert fillInBlankExpert, ExecutorService executorService, int maxDiscrepancyCount, boolean bailOnEmpty) {
        this.fillInBlankExpert = fillInBlankExpert;
//        this.executorService = Executors.newFixedThreadPool(threadCount);
        this.executorService = executorService;
        this.maxDiscrepancyCount = maxDiscrepancyCount;
        this.bailOnEmpty = bailOnEmpty;
    }



    public static final class NoOpPuzzleListener implements PuzzleListener {
        @Override
        public void currentPuzzle(CWPuzzle puzzle) {
            // no-op
        }
    }

    /**
     * @param startPuzzle
     * @return a new instance of the puzzle with the clues "solved"
     */
    @Override
    public CWPuzzle solvePuzzle(final CWPuzzle startPuzzle, final PuzzleListener puzzleListener) {
        try {
            CWPuzzle localPuzzle = startPuzzle;
            while(!getUnsolvedClues(localPuzzle).isEmpty()) {
                CWPuzzle local2Puzzle = solvePuzzleImpl(localPuzzle, puzzleListener);

                if(localPuzzle.getMissingLetters() == local2Puzzle.getMissingLetters()) {
                    break;
                } else {
                    localPuzzle = local2Puzzle;
                }
            }

            return localPuzzle;
        } finally {
            if(fillInBlankExpert instanceof Closeable) {
                try {
                    ((Closeable)fillInBlankExpert).close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    private CWPuzzle solvePuzzleImpl(CWPuzzle startPuzzle, final PuzzleListener puzzleListener) {
        WeightedPuzzle bestAnswer = null;

        TreeSet<WeightedPuzzle> workSet = new TreeSet<WeightedPuzzle>();
        workSet.add(WeightedPuzzle.initialize(startPuzzle));

        while(!workSet.isEmpty()) {

            final WeightedPuzzle workingAnswer = workSet.pollFirst();
            if(workingAnswer.getDiscrepancyCount() > maxDiscrepancyCount) {
                continue;
            }

            CWPuzzle puzzleState = workingAnswer.getPuzzleState();
            puzzleListener.currentPuzzle(puzzleState);

            List<CWClue> unsolvedClues = getUnsolvedClues(puzzleState);

            // if we have one that's filled in, then we're done!
            if(unsolvedClues.isEmpty()) {
                return puzzleState;
            }

            List<SearchWrapper> searchWrappers = getClueSearches(puzzleState, workingAnswer.getCluesSolved() == 0, unsolvedClues);
            searchWrappers = new ArrayList<SearchWrapper>(Collections2.filter(searchWrappers, new Predicate<SearchWrapper>() {
                @Override
                public boolean apply(SearchWrapper input) {
                    return input != null && input.getBestAnswers() != null;
                }
            }));
            Collections.sort(searchWrappers);

            boolean first = true;
            for (SearchWrapper searchWrapper : Lists.reverse(searchWrappers)) {
                for (String potentialAnswer : searchWrapper.getBestAnswers()) {
                    WeightedPuzzle weightedPuzzle = workingAnswer.performUpdate(searchWrapper.getClue(), potentialAnswer, first, searchWrapper.getBestScore());
                    if(weightedPuzzle != null) {
                        workSet.add(weightedPuzzle);
                    }
                    first = false;
                }
            }
            if(workingAnswer.betterFit(bestAnswer)) {
                bestAnswer = workingAnswer;
            }
        }

        CWPuzzle bestPuzzle = bestAnswer == startPuzzle ? startPuzzle : bestAnswer.getPuzzleState();
        puzzleListener.currentPuzzle(bestPuzzle);
        return bestPuzzle;
    }

    private List<CWClue> getUnsolvedClues(CWPuzzle startPuzzle) {
        List<CWClue> unsolvedClues = new ArrayList<CWClue>();

        for (CWClue cwClue : startPuzzle.getAllClues()) {
            if(!startPuzzle.isClueSolved(cwClue)) {
                unsolvedClues.add(cwClue);
            }
        }
        return unsolvedClues;
    }

    private List<SearchWrapper> getClueSearches(final CWPuzzle startPuzzle, boolean initialPuzzle, List<CWClue> unsolvedClues) {
        List<Future<Map<String, Long>>> futures = new ArrayList<Future<Map<String, Long>>>();
        for (final CWClue unsolvedClue : unsolvedClues) {
            if(initialPuzzle || startPuzzle.intersectsSolvedClue(unsolvedClue)) {
                futures.add(executorService.submit(new Callable<Map<String, Long>>() {
                    @Override
                    public Map<String, Long> call() throws Exception {
                        String clueState = startPuzzle.getClueState(unsolvedClue);
                        return fillInBlankExpert.performSearch(unsolvedClue, clueState);
                    }
                }));
            } else {
                futures.add(executorService.submit(new Callable<Map<String, Long>>() {
                    @Override
                    public Map<String, Long> call() throws Exception {
                        return null;
                    }
                }));
            }
        }
        List<Optional<Map<String, Long>>> scoredResults = new ArrayList<Optional<Map<String,Long>>>();
        for (Future<Map<String, Long>> future : futures) {
            try {
//                scoredResults.add(Optional.of(future.get(10, TimeUnit.SECONDS)));
                scoredResults.add(Optional.fromNullable(future.get()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                scoredResults.add(Optional.<Map<String, Long>>absent());
                // swallow and move on!
                /*if(e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException)e.getCause();
                }
                throw new RuntimeException(e.getCause());*/
            } /*catch (TimeoutException e) {
                // ignore
                System.out.println("Timed out");
                scoredResults.add(Optional.<Map<String, Long>>absent());
            }*/
        }

        List<SearchWrapper> searchWrappers = new ArrayList<SearchWrapper>();

        for (int i = 0; i < scoredResults.size(); i++) {
            Optional<Map<String, Long>> scoredResult = scoredResults.get(i);
            if(scoredResult.isPresent()) {
                Map<String, Long> scoreMap = scoredResult.get();

                if(bailOnEmpty && scoreMap.isEmpty()) {
                    return Collections.emptyList();
                }

                CWClue unsolvedClue = unsolvedClues.get(i);
                int filledLetters = startPuzzle.filledLetters(unsolvedClue);

                List<Map.Entry<String, Long>> entries = new ArrayList<Map.Entry<String, Long>>(scoreMap.entrySet());
                Collections.sort(entries, new Comparator<Map.Entry<String, Long>>() {
                    @Override
                    public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                        int compare = o2.getValue().compareTo(o1.getValue());
                        if(compare == 0) {
                            return o1.getKey().compareTo(o2.getKey());
                        }
                        return compare;
                    }
                });

                for(int j = 0; j < scoreMap.size() && j < 10; j++) { // only do the top 10
                    Map.Entry<String, Long> localEntry = entries.get(j);
                    searchWrappers.add(new SearchWrapper(unsolvedClue, filledLetters, Collections.singletonMap(localEntry.getKey(), localEntry.getValue())){
                        @Override
                        public int compareTo(SearchWrapper o) {
                            if(singleAnswer ^ o.singleAnswer) {
                                return singleAnswer ? Integer.MAX_VALUE : Integer.MIN_VALUE;
                            } else {
                                if(bestScore.equals(o.bestScore)) {
                                    return filledLetters - o.filledLetters;
                                } else {
                                    return bestScore.compareTo(o.bestScore);
                                }
                            }
                        }
                    });
                }
            }
        }

        if(initialPuzzle) {
            System.out.println("INIT DONE =============================================================");
        }

        return searchWrappers;
    }
}
