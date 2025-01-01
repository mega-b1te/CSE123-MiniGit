import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;



public class Testing {
    private Repository repo1;
    private Repository repo2;

    // Occurs before each of the individual test cases
    // (creates new repos and resets commit ids)
    @BeforeEach
    public void setUp() {
        repo1 = new Repository("repo1");
        repo2 = new Repository("repo2");
        Repository.Commit.resetIds();
    }

    // TODO: Write your tests here!


    // Behavior: 
    //      - tests the history command if the n given is a plausible value (> 0 and in this case
    //      - <= size of the repository)
    // Exceptions:
    //      - Throws an InterruptedException in case the test is interrupted 
    @Test
    @DisplayName("history() - n is valid")
    void testingValidHistoryCommand() throws InterruptedException{
        commitAll(repo1, new String[]{"One", "Two"});
        assertEquals(2, repo1.getRepoSize());

        testHistory(repo1, 2, new String[]{"One", "Two"});
    }

    // Behavior: 
    //      - tests the history command if the n given is an invalid value (<= 0)
    // Exceptions:
    //      - Throws an InterruptedException in case the test is interrupted 
    //      - An IllegalArgumentException is thrown by the getHistory() method since
    //      - n <= 0
    @Test
    @DisplayName("history() - n is invalid")
    void testingInvalidHistoryCommand() throws InterruptedException{
        commitAll(repo1, new String[]{"One", "Two"});
        assertEquals(2, repo1.getRepoSize());

        assertThrows(IllegalArgumentException.class, () -> {
            testHistory(repo1, 0, new String[]{"One", "Two"});
        });
    
        assertThrows(IllegalArgumentException.class, () -> {
            testHistory(repo1, -10, new String[]{"One", "Two"});
        });      
    }


       
    // Behavior: 
    //      - tests the drop command to see if the first value is being dropped properly
    //      - as well as if the a commit id provided isn't contained in the repository
    // Exceptions:
    //      - Throws an InterruptedException in case the test is interrupted 
    @Test
    @DisplayName("drop() - front case")
    void testingDropping() throws InterruptedException{
        commitAll(repo1, new String[]{"Zero", "One", "Two", "Three"});
        commitAll(repo2, new String[]{"Four"});

        assertEquals(4, repo1.getRepoSize());
        assertEquals(1, repo2.getRepoSize());

        assertTrue(repo1.drop("0"));
        assertEquals(3, repo1.getRepoSize());

        assertFalse(repo2.drop("3"));
        assertTrue(repo2.drop("4"));
        assertEquals(0, repo2.getRepoSize());
    }


    // Behavior: 
    //      - tests the synchronize command to see if it can handle adding a value with a
    //      - larger timestamp from one repository (repo1) into another repository (repo2) that 
    //      - all values with smaller timestamps (adding to the beginning)
    // Exceptions:
    //      - Throws an InterruptedException in case the test is interrupted 
    @Test
    @DisplayName("synchronize() (repo1: [4], repo2: [1, 2, 3] / repo1 into repo2)")
    public void testSynchronizeOne() throws InterruptedException {
        commitAll(repo2, new String[]{"One", "Two", "Three"});
        commitAll(repo1, new String[]{"Four"});
        
        assertEquals(1, repo1.getRepoSize());
        assertEquals(3, repo2.getRepoSize());

        repo2.synchronize(repo1);
        assertEquals(0, repo1.getRepoSize());
        assertEquals(4, repo2.getRepoSize());

        testHistory(repo2, 4, new String[]{"One", "Two", "Three", "Four"});
    }

    // Behavior: 
    //      - tests the synchronize command to see if it can handle adding values with smaller
    //      - timestamps from one repository (repo2) into another repository (repo1) that has
    //      - a value with a larger timestamp than all the rest (adding to the end)
    // Exceptions:
    //      - Throws an InterruptedException in case the test is interrupted 
    @Test
    @DisplayName("synchronize() (repo1: [4], repo2: [1, 2, 3] / repo2 into repo1)")
    public void testSynchronizeTwo() throws InterruptedException {
        commitAll(repo2, new String[]{"One", "Two", "Three"});
        commitAll(repo1, new String[]{"Four"});
        
        assertEquals(1, repo1.getRepoSize());
        assertEquals(3, repo2.getRepoSize());

        repo1.synchronize(repo2);
        assertEquals(4, repo1.getRepoSize());
        assertEquals(0, repo2.getRepoSize());

        testHistory(repo1, 4, new String[]{"One", "Two", "Three", "Four"});
    }

    

    // Behavior: 
    //      - tests the synchronize command to see if it can handle mixed timestamp values in
    //      - each of the repositories (some are larger timestamps and some are 
    //      - in each repository smaller timestamps)
    // Exceptions:
    //      - Throws an InterruptedException in case the test is interrupted 
    @Test
    @DisplayName("synchronize() (repo1: [1, 4, 5], repo2: [2, 3, 6, 7] / repo2 into repo1)")
    public void testSynchronizeThree() throws InterruptedException {
        commitAll(repo1, new String[]{"One"});
        commitAll(repo2, new String[]{"Two", "Three"});
        commitAll(repo1, new String[]{"Four", "Five"});
        commitAll(repo2, new String[]{"Six", "Seven"});
        
        assertEquals(3, repo1.getRepoSize());
        assertEquals(4, repo2.getRepoSize());

        repo1.synchronize(repo2);
        assertEquals(7, repo1.getRepoSize());
        assertEquals(0, repo2.getRepoSize());

        testHistory(repo1, 7, new String[]{"One", "Two", "Three", "Four", "Five", "Six", "Seven"});
    }

    // Behavior: 
    //      - tests the synchronize command to see if it can handle mixed timestamp values in
    //      - each of the repositories (some are larger timestamps and some are 
    //      - in each repository smaller timestamps) and makes sure that the synchronize command
    //      - provides the same commit history when synchronizing the same repositories (but
    //      - instead of repo2 into repo1, doing repo1 into repo2)
    // Exceptions:
    //      - Throws an InterruptedException in case the test is interrupted 
    @Test
    @DisplayName("synchronize() (repo1: [1, 4, 5], repo2: [2, 3, 6, 7] / repo1 into repo2)")
    public void testSynchronizeFour() throws InterruptedException {
        commitAll(repo1, new String[]{"One"});
        commitAll(repo2, new String[]{"Two", "Three"});
        commitAll(repo1, new String[]{"Four", "Five"});
        commitAll(repo2, new String[]{"Six", "Seven"});
        
        assertEquals(3, repo1.getRepoSize());
        assertEquals(4, repo2.getRepoSize());

        repo2.synchronize(repo1);
        assertEquals(0, repo1.getRepoSize());
        assertEquals(7, repo2.getRepoSize());

        testHistory(repo2, 7, 
                new String[]{"One", "Two", "Three", "Four", "Five", "Six", "Seven"});
    }

    // Behavior: 
    //      - tests the synchronize command to see if it can handle adding a value with a
    //      - smaller timestamp from one repository (repo1) into another repository (repo2) that 
    //      - contains all values with larger timestamps (adding to the end)
    // Exceptions:
    //      - Throws an InterruptedException in case the test is interrupted 
    @Test
    @DisplayName("synchronize() (repo1: [1], repo2: [2, 3] / repo2 into repo1)")
    public void testSynchronizeFive() throws InterruptedException {
        commitAll(repo1, new String[]{"One"});
        commitAll(repo2, new String[]{"Two", "Three"});
        
        assertEquals(1, repo1.getRepoSize());
        assertEquals(2, repo2.getRepoSize());

        repo2.synchronize(repo1);
        assertEquals(0, repo1.getRepoSize());
        assertEquals(3, repo2.getRepoSize());

        testHistory(repo2, 3, new String[]{"One", "Two", "Three"});
    }

    // Behavior: 
    //      - tests the synchronize command to see if it can handle adding values with a
    //      - larger timestamp from one repository (repo2) into another repository (repo1) that 
    //      - contains a value with a smaller timestamp (adding to the beginning)
    // Exceptions:
    //      - Throws an InterruptedException in case the test is interrupted 
    @Test
    @DisplayName("synchronize() (repo1: [1], repo2: [2, 3] / repo1 into repo2)")
    public void testSynchronizeSix() throws InterruptedException {
        commitAll(repo1, new String[]{"One"});
        commitAll(repo2, new String[]{"Two", "Three"});
        
        assertEquals(1, repo1.getRepoSize());
        assertEquals(2, repo2.getRepoSize());

        repo1.synchronize(repo2);
        assertEquals(3, repo1.getRepoSize());
        assertEquals(0, repo2.getRepoSize());

        testHistory(repo1, 3, new String[]{"One", "Two", "Three"});
    }

    // Behavior: 
    //      - tests the synchronize command to see how it handles when both repositories given
    //      - are empty (shouldn't change the repositories at all)
    // Exceptions:
    //      - Throws an InterruptedException in case the test is interrupted 
    @Test
    @DisplayName("synchronize() (repo1: null, repo2: null / repo2 into repo1)")
    public void testSynchronizeSeven() throws InterruptedException {
        
        assertEquals(0, repo1.getRepoSize());
        assertEquals(0, repo2.getRepoSize());

        repo1.synchronize(repo2);
        assertEquals(0, repo1.getRepoSize());
        assertEquals(0, repo2.getRepoSize());

        assertEquals(null, repo1.getRepoHead());
        assertEquals(null, repo2.getRepoHead());
    }

    // Behavior: 
    //      - tests the synchronize command to see how it handles when the repository (repo1) 
    //      - that has commits being combined into it is empty, but the other repository (repo2)
    //      - that is providing commits isn't empty (should add all the commits from
    //      - repo2 into repo1)
    // Exceptions:
    //      - Throws an InterruptedException in case the test is interrupted 
    @Test
    @DisplayName("synchronize() (repo1: null, repo2: [1, 2] / repo2 into repo1)")
    public void testSynchronizeEight() throws InterruptedException {

        commitAll(repo2, new String[]{"One", "Two"});

        assertEquals(0, repo1.getRepoSize());
        assertEquals(2, repo2.getRepoSize());

        repo1.synchronize(repo2);
        assertEquals(2, repo1.getRepoSize());
        assertEquals(0, repo2.getRepoSize());

        testHistory(repo1, 2, new String[]{"One", "Two"});

    }

    // Behavior: 
    //      - tests the synchronize command to see how it handles when the repository (repo1) 
    //      - that has commits being combined into it isn't empty, but the other 
    //      - repository (repo2) that is providing commits is empty (shouldn't change 
    //      - the repositories at all)
    // Exceptions:
    //      - Throws an InterruptedException in case the test is interrupted 
    @Test
    @DisplayName("synchronize() (repo1: null, repo2: [1, 2] / repo1 into repo2)")
    public void testSynchronizeNine() throws InterruptedException {

        commitAll(repo2, new String[]{"One", "Two"});

        assertEquals(0, repo1.getRepoSize());
        assertEquals(2, repo2.getRepoSize());

        repo2.synchronize(repo1);
        assertEquals(0, repo1.getRepoSize());
        assertEquals(2, repo2.getRepoSize());

        testHistory(repo2, 2, new String[]{"One", "Two"});

    }



    /////////////////////////////////////////////////////////////////////////////////
    // PROVIDED HELPER METHODS (You don't have to use these if you don't want to!) //
    /////////////////////////////////////////////////////////////////////////////////

    // Commits all of the provided messages into the provided repo, making sure timestamps
    // are correctly sequential (no ties). If used, make sure to include
    //      'throws InterruptedException'
    // much like we do with 'throws FileNotFoundException'. Example useage:
    //
    // repo1:
    //      head -> null
    // To commit the messages "one", "two", "three", "four"
    //      commitAll(repo1, new String[]{"one", "two", "three", "four"})
    // This results in the following after picture
    // repo1:
    //      head -> "four" -> "three" -> "two" -> "one" -> null
    //
    // YOU DO NOT NEED TO UNDERSTAND HOW THIS METHOD WORKS TO USE IT! (this is why documentation
    // is important!)
    public void commitAll(Repository repo, String[] messages) throws InterruptedException {
        // Commit all of the provided messages
        for (String message : messages) {
            int size = repo.getRepoSize();
            repo.commit(message);
            
            // Make sure exactly one commit was added to the repo
            assertEquals(size + 1, repo.getRepoSize(),
                         String.format("Size not correctly updated after commiting message [%s]",
                                       message));

            // Sleep to guarantee that all commits have different time stamps
            Thread.sleep(2);
        }
    }

    // Makes sure the given repositories history is correct up to 'n' commits, checking against
    // all commits made in order. Example useage:
    //
    // repo1:
    //      head -> "four" -> "three" -> "two" -> "one" -> null
    //      (Commits made in the order ["one", "two", "three", "four"])
    // To test the getHistory() method up to n=3 commits this can be done with:
    //      testHistory(repo1, 3, new String[]{"one", "two", "three", "four"})
    // Similarly, to test getHistory() up to n=4 commits you'd use:
    //      testHistory(repo1, 4, new String[]{"one", "two", "three", "four"})
    //
    // YOU DO NOT NEED TO UNDERSTAND HOW THIS METHOD WORKS TO USE IT! (this is why documentation
    // is important!)
    public void testHistory(Repository repo, int n, String[] allCommits) {
        int totalCommits = repo.getRepoSize();
        assertTrue(n <= totalCommits,
                   String.format("Provided n [%d] too big. Only [%d] commits",
                                 n, totalCommits));
        
        String[] nCommits = repo.getHistory(n).split("\n");
        
        assertTrue(nCommits.length <= n,
                   String.format("getHistory(n) returned more than n [%d] commits", n));
        assertTrue(nCommits.length <= allCommits.length,
                   String.format("Not enough expected commits to check against. " +
                                 "Expected at least [%d]. Actual [%d]",
                                 n, allCommits.length));
        
        for (int i = 0; i < n; i++) {
            String commit = nCommits[i];

            // Old commit messages/ids are on the left and the more recent commit messages/ids are
            // on the right so need to traverse from right to left
            int backwardsIndex = totalCommits - 1 - i;
            String commitMessage = allCommits[backwardsIndex];

            assertTrue(commit.contains(commitMessage),
                       String.format("Commit [%s] doesn't contain expected message [%s]",
                                     commit, commitMessage));
            assertTrue(commit.contains("" + backwardsIndex),
                       String.format("Commit [%s] doesn't contain expected id [%d]",
                                     commit, backwardsIndex));
        }
    }
}
