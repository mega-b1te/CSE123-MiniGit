import java.util.*;
import java.text.SimpleDateFormat;

//Represents a repository that contains commits (ordering them based on the time they were 
//committed) that can be manipulated (add commits, delete commits, see all commits, put commits 
//into another repository)
public class Repository {

    /**
     * TODO: Implement your code here.
     */

    private String repositoryName;
    private Commit repositoryHead;
    private int size;

    // Behavior: 
    //      - creates a new, empty, repository
    // Exceptions:
    //      - Throws an IllegalArgumentException if the client enters a name that is empty
    //      - or the provided name is null
    // Parameter:
    //      - 'name': the name of the created repository
    public Repository(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException();
        }

        repositoryName = name;
        size = 0;
    } 

    // Behavior: 
    //      - provides the client with the id of the most recent commit in the repo
    // Returns: 
    //      - String: id of most recent commit (null if there are no commits in the repo)
    public String getRepoHead() {
        if (repositoryHead == null) {
            return null;
        }

        return repositoryHead.id;
    }

    // Behavior: 
    //      - provides the size of the repository (number of commits)
    // Returns: 
    //      - int: repository size
    public int getRepoSize() {
        return size;
    }

    // Behavior: 
    //      - creates a representation of the repository by detailing the name of the repo
    //      - and the current head of the repository
    // Returns: 
    //      - String: representation of the repository and its head 
    public String toString() {
        String stringRepresentation = repositoryName + " - ";

        if (repositoryHead == null) {
            return stringRepresentation + "No commits";
        }

        return stringRepresentation + "Current head: " + repositoryHead.toString();
    }

    // Behavior: 
    //      - checks if the repository contains a certain commit using its id
    // Parameter:
    //      - 'targetId': the id of the commit that we want to check for in the repository
    // Returns: 
    //      - boolean: true if the commit with the corresponding id is contained within the
    //      - repository and false if it isn't contained in the repository
    public boolean contains(String targetId) {
        Commit tempReference = repositoryHead;

        while (tempReference != null) {
            if (tempReference.id.equals(targetId)) {
                return true;
            }

            tempReference = tempReference.past;
        }

        return false;
    }

    // Behavior: 
    //      - returns a list of commits in the repository (most recent to least recent) based
    //      - on the number of commits that the client provides they want to see (if that number
    //      - is greater than the size of the repository, then the history representation will
    //      - just include all of the commits in the repository)
    // Exceptions:
    //      - Throws an IllegalArgumentException if the client enters a number of commits they
    //      - want to see in the history that is less than or equal to zero
    // Parameter:
    //      - 'n': the number of commits in the history that the client wants to see
    // Returns: 
    //      - String: a representation of the history of the repository up until 'n' commits 
    public String getHistory(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException();
        }

        String history = "";
        Commit tempReference = repositoryHead;


        while (tempReference != null && n > 0) {
            history += tempReference.toString() + "\n";
            tempReference = tempReference.past;
            n--;
        }

        return history;
    }

    // Behavior: 
    //      - adds a commit to the repository as the head of the repository (most recent commit)
    // Parameter:
    //      - 'message': the client provided message that will be part of the commit
    // Returns: 
    //      - String: the id of the most recent created commit 
    public String commit(String message) {
        Commit newHead = new Commit(message, repositoryHead);
        repositoryHead = newHead;
        size++;
        return repositoryHead.id;
    }

    // Behavior: 
    //      - removes a commit from the repository if the commit is present
    // Parameter:
    //      - 'targetId': the id of the commit that we want to remove from the repository
    // Returns: 
    //      - boolean: true if the drop was successful (commit was present in the repository
    //      - and was removed) and false if the drop was unsuccesful (commit wasn't present in
    //      - the repository so nothing was removed)
    public boolean drop(String targetId) {
        if (repositoryHead == null) {
            return false;
        }

        if (repositoryHead.id.equals(targetId)) {
            repositoryHead = repositoryHead.past;
            size--;
            return true;
        }

        Commit tempReference = repositoryHead;

        while (tempReference.past != null) {
            if (tempReference.past.id.equals(targetId)) {
                tempReference.past = tempReference.past.past;
                size--;
                return true;
            }

            tempReference = tempReference.past;
        }

        return false;
    }

    // Behavior: 
    //      - combines another repository with this repository by adding all the
    //      - commits from the other repository into this repository and ordering them by
    //      - which one is most recent (the other repository is empty after this action)
    // Parameter:
    //      - 'other': the repository whose commits we want to combine into this repository (we
    //                  are assuming that other is non-null)
    public void synchronize(Repository other) {
        if (this.repositoryHead != null && other.repositoryHead != null) {
            Commit iterationReference = this.repositoryHead; //iterates in this repository
            Commit priorIterationReference = iterationReference;//adds commits to this repository
            this.size += other.size;

            //Comparing head values separate from the rest and readjusting if needed
            if (other.repositoryHead.timeStamp > this.repositoryHead.timeStamp) {
                this.repositoryHead = other.repositoryHead;
                other.repositoryHead = other.repositoryHead.past;
                this.repositoryHead.past = iterationReference;
                priorIterationReference = this.repositoryHead;
            }

            while (iterationReference != null && other.repositoryHead != null) {
                if (iterationReference.timeStamp > other.repositoryHead.timeStamp) {

                    //end of the current repository but the other one still has more commits
                    if (iterationReference.past == null) {
                        iterationReference.past = other.repositoryHead;
                        other.repositoryHead = null;
                    } else {
                        iterationReference = iterationReference.past;

                        //to make sure that iteration and priorIteration don't refer to the same
                        //thing while iterating
                        if (iterationReference.equals(priorIterationReference.past.past)) {
                            priorIterationReference = priorIterationReference.past;
                        }
                    }   
                }

                if (other.repositoryHead != null && 
                        other.repositoryHead.timeStamp > iterationReference.timeStamp) {
                    priorIterationReference.past = other.repositoryHead;
                    other.repositoryHead = other.repositoryHead.past;
                    priorIterationReference.past.past = iterationReference;
                    priorIterationReference = priorIterationReference.past;

                }
            }
        //nothing inside of current repository but other repository has commits
        } else if (this.repositoryHead == null && other.repositoryHead != null) {
            this.repositoryHead = other.repositoryHead;
            other.repositoryHead = null;
            this.size = other.size;
        }

        other.size = 0;
    }

    /**
     * DO NOT MODIFY
     * A class that represents a single commit in the repository.
     * Commits are characterized by an identifier, a commit message,
     * and the time that the commit was made. A commit also stores
     * a reference to the immediately previous commit if it exists.
     *
     * Staff Note: You may notice that the comments in this 
     * class openly mention the fields of the class. This is fine 
     * because the fields of the Commit class are public. In general, 
     * be careful about revealing implementation details!
     */
    public static class Commit {

        private static int currentCommitID;

        /**
         * The time, in milliseconds, at which this commit was created.
         */
        public final long timeStamp;

        /**
         * A unique identifier for this commit.
         */
        public final String id;

        /**
         * A message describing the changes made in this commit.
         */
        public final String message;

        /**
         * A reference to the previous commit, if it exists. Otherwise, null.
         */
        public Commit past;

        /**
         * Constructs a commit object. The unique identifier and timestamp
         * are automatically generated.
         * @param message A message describing the changes made in this commit. Should be non-null.
         * @param past A reference to the commit made immediately before this
         *             commit.
         */
        public Commit(String message, Commit past) {
            this.id = "" + currentCommitID++;
            this.message = message;
            this.timeStamp = System.currentTimeMillis();
            this.past = past;
        }

        /**
         * Constructs a commit object with no previous commit. The unique
         * identifier and timestamp are automatically generated.
         * @param message A message describing the changes made in this commit. Should be non-null.
         */
        public Commit(String message) {
            this(message, null);
        }

        /**
         * Returns a string representation of this commit. The string
         * representation consists of this commit's unique identifier,
         * timestamp, and message, in the following form:
         *      "[identifier] at [timestamp]: [message]"
         * @return The string representation of this collection.
         */
        @Override
        public String toString() {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
            Date date = new Date(timeStamp);

            return id + " at " + formatter.format(date) + ": " + message;
        }

        /**
        * Resets the IDs of the commit nodes such that they reset to 0.
        * Primarily for testing purposes.
        */
        public static void resetIds() {
            Commit.currentCommitID = 0;
        }
    }
}
