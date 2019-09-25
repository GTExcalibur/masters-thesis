/*
 * Copyright (c) 2008 Intelligent Software Solutions
 * Unpublished-all rights reserved under the copyright laws of the
 * United States.
 *
 * This software was developed under sponsorship from the
 * Air Force Research Laboratory under FA8750-06-D-005.
 *
 * Contractor: Intelligent Software Solutions,
 * 5450 Tech Center Drive, Suite 400, Colorado Springs, 80919.
 * http://www.issinc.com
 * Expiration Date: June 2015
 *
 * Intelligent Software Solutions has title to the rights in this computer
 * software. The Government's rights to use, modify, reproduce, release,
 * perform, display, or disclose these technical data are restricted by
 * paragraph (b)(2) of the Rights in Technical Data-Noncommercial Items
 * clause contained in Contract No. FA8750-06-D-005. No restrictions to the
 * Government apply after the expiration date shown above. Any
 * reproduction of technical data or portions thereof marked with this
 * legend must also reproduce the markings.
 *
 * Intelligent Software Solutions does not grant permission inconsistent with
 * the aforementioned unlimited government rights to use, disclose, copy,
 * or make derivative works of this software to parties outside the
 * Government.
 */

package gturner.crossword.junit;

import gturner.crossword.impl.CWPuzzleFactory;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;

/**
 * Created by IntelliJ IDEA.
 * <br> Date: 2/26/11, Time: 8:26 AM
 * <br> To change this template use File | Settings | File Templates.
 *
 * @author George.Turner
 */
public class PuzzleFactoryTest extends TestCase {
    private final File rootTestDirectory = new File("C:\\Users\\George Turner\\IdeaProjects\\CS 582 Main Project\\crossword-parent\\crossword-core-engine\\src\\test\\resources");

    public void test_Aug0497_read_case() throws Exception {
        CWPuzzleFactory.createPuzzleFromInputStream(Thread.currentThread().
                getContextClassLoader().getResourceAsStream("puz_format/nytimes/Aug0497.puz"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);
    }

    public void test_May2297_read_case() throws Exception {
        CWPuzzleFactory.createPuzzleFromInputStream(Thread.currentThread().
                getContextClassLoader().getResourceAsStream("puz_format/nytimes/May2297.puz"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);
    }

    public void test_barelybad_read_case() throws Exception {
        loadFilesInDirectory(new File(rootTestDirectory, "puz_format\\barelybad"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);
    }

    public void test_bobklahn_read_case() throws Exception {
        loadFilesInDirectory(new File(rootTestDirectory, "puz_format\\bobklahn"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);
    }

    public void test_chron_read_case() throws Exception {
        loadFilesInDirectory(new File(rootTestDirectory, "puz_format\\chron"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);
    }

    public void test_fleetingimage_read_case() throws Exception {
        loadFilesInDirectory(new File(rootTestDirectory, "puz_format\\fleetingimage"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);
    }

    public void test_houstonChronicle_read_case() throws Exception {
        loadFilesInDirectory(new File(rootTestDirectory, "puz_format\\houstonChronicle"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);
    }

    public void test_macnamarasband_read_case() throws Exception {
        loadFilesInDirectory(new File(rootTestDirectory, "puz_format\\macnamarasband"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);
    }

    public void test_nytimes_read_case() throws Exception {
        loadFilesInDirectory(new File(rootTestDirectory, "puz_format\\nytimes"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);
    }

    public void test_thinksCom_read_case() throws Exception {
        loadFilesInDirectory(new File(rootTestDirectory, "puz_format\\thinksCom"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);
    }

    public void test_washingtonPost_read_case() throws Exception {
        loadFilesInDirectory(new File(rootTestDirectory, "puz_format\\washingtonPost"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);
    }

    public void test_xwordinfo_read_case() throws Exception {
        loadFilesInDirectory(new File(rootTestDirectory, "xpf_format\\xwordinfo"), CWPuzzleFactory.FILE_TYPES.XPUF_EXT);
    }

    public void test_thesis_test_data() throws Exception {
        loadFilesInDirectory(new File("F:\\dev\\thesis-test-data"), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);
    }

    public void test_thesis_test_data_specific() throws Exception {
        CWPuzzleFactory.createPuzzleFromInputStream(new FileInputStream(new File("F:\\dev\\thesis-test-data\\5x5\\4.puz")), CWPuzzleFactory.FILE_TYPES.PUZ_EXT);
    }

    private void loadFilesInDirectory(File directory, final CWPuzzleFactory.FILE_TYPES type) throws Exception {
        File[] files = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                File localFile = new File(dir, name);
                return localFile.isDirectory() || name.endsWith(type.getExtension());
            }
        });
        for (File file : files) {
            if(file.isDirectory()) {
                loadFilesInDirectory(file, type);
            } else {
                try {
                    CWPuzzleFactory.createPuzzleFromInputStream(new FileInputStream(file), type);
                } catch (Exception e) {
                    System.out.println("file had error: " + file.getAbsolutePath());
                    throw e;
                }
            }
        }
    }
}
