package gturner.solver.impl;

import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
 * Date: 9/24/14
 * Time: 8:51 AM
 * To change this template use File | Settings | File Templates.
 */
public final class PuzzleSolver implements IPuzzleSolver {

    private final IClueExpert fillInBlankExpert;
    private final ExecutorService executorService;
    private final int maxDiscrepancyCount;


    public PuzzleSolver(IClueExpert fillInBlankExpert, ExecutorService executorService, int maxDiscrepancyCount) {
        this.fillInBlankExpert = fillInBlankExpert;
//        this.executorService = Executors.newFixedThreadPool(threadCount);
        this.executorService = executorService;
        this.maxDiscrepancyCount = maxDiscrepancyCount;
    }

    /**
     * @param startPuzzle
     * @return a new instance of the puzzle with the clues "solved"
     */
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
        List<Future<List<SearchWrapper>>> futures = new ArrayList<Future<List<SearchWrapper>>>();
        for (final CWClue unsolvedClue : unsolvedClues) {
            if(initialPuzzle || startPuzzle.intersectsSolvedClue(unsolvedClue)) {
                futures.add(executorService.submit(new Callable<List<SearchWrapper>>() {
                    @Override
                    public List<SearchWrapper> call() throws Exception {
                        String clueState = startPuzzle.getClueState(unsolvedClue);
                        return getScoredResults(unsolvedClue, startPuzzle.filledLetters(unsolvedClue), clueState);
                    }
                }));
            }
        }
        List<SearchWrapper> searchWrappers = new ArrayList<SearchWrapper>();
        for (Future<List<SearchWrapper>> future : futures) {
            try {
                searchWrappers.addAll(future.get(10, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                // swallow and move on!
                /*if(e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException)e.getCause();
                }
                throw new RuntimeException(e.getCause());*/
            } catch (TimeoutException e) {
                // ignore
                System.out.println("Timed out");
            }
        }
        return searchWrappers;
    }

    private List<SearchWrapper> getScoredResults(final CWClue clue, final int filledLetters, final String state) {
        return Collections.singletonList(new SearchWrapper(clue, filledLetters, fillInBlankExpert.performSearch(clue, state)));
    }
}
