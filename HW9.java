import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;

/**
 * Kevin Hu - CS 170 Homework 9
 */
public class HW9 {

    public static class Read {
        HashMap<Integer, HashSet<Read>> backOverlap;
        HashMap<Integer, HashSet<Read>> frontOverlap;
        String read;
        public Read(String read) {
            this.read = read;
            this.backOverlap = new HashMap<Integer, HashSet<Read>>();
            this.frontOverlap = new HashMap<Integer, HashSet<Read>>();
        }
    }

    public static String algorithm(List<String> substrings) {
        // Given list of n substrings

        ArrayList<Read> reads = new ArrayList<Read>();
        Boolean[] isSubstring = new Boolean[substrings.size()];
        for (int i = 0; i < substrings.size(); i++) {
            isSubstring[i] = false;
        }

        // Find all reads that are substrings of other reads
        for (int i = 0; i < substrings.size(); i++) {
            for (int j = i+1; j < substrings.size() - 1; j++) {
                // for every substring s, find the other substring t s.t.
                // where s[length(s)-1-x, length(s)-1] == t[0, x] and x is a maximum
                if (i != j) {
                    String s = substrings.get(i);
                    String t = substrings.get(j);
                    if (s.length() < t.length() && t.contains(s)) {
                        isSubstring[i] = true;
                    } else if (s.length() > t.length() && s.contains(t)) {
                        isSubstring[j] = true;
                    } else if (s.equals(t)) {
                        isSubstring[i] = true;
                    }
                }
            }
        }

        // Only add reads that are not substrings of other reads
        for (int i =0; i < isSubstring.length; i++) {
            if (!isSubstring[i]) {
                Read newRead = new Read(substrings.get(i));
                reads.add(newRead);
            }
        }

        int totalMaxOverlap = 0;
        Read totalFront = null;
        Read totalBack = null;
        for (int i = 0; i < reads.size(); i++) {
            Read s = reads.get(i);
            int slength = s.read.length();
            for (int j = i+1; j < reads.size()-1; j++) {
                Read big = null;
                Read small = null;
                Read t = reads.get(j);
                int tlength = t.read.length();
                if (slength >= tlength) {
                    big = s;
                    small = t;
                } else {
                    big = t;
                    small = s;
                }

                String smallRead = small.read;
                String bigRead = big.read;
                int smallL = smallRead.length();
                int bigL = bigRead.length();

                int maxBigBackOverlap = 0; // = maxSmallFrontOverlap
                int maxBigFrontOverlap = 0; // = maxSmallBackOverlap

                // Check the longest # of chars that the 2 strings overlap by on both ends of the strings
                for (int k = 1; k < smallL; k++) {
                    if (smallRead.substring(0, k).equals(bigRead.substring(bigL-k, bigL))) {
                        if (k > maxBigBackOverlap) {
                            maxBigBackOverlap = k;
                        }
                    }
                    if (smallRead.substring(smallL-k, smallL).equals(bigRead.substring(0, k))) {
                        if (k > maxBigFrontOverlap) {
                            maxBigFrontOverlap = k;
                        }
                    }
                }

                if (maxBigBackOverlap > 0) {
                    // add it to big's backoverlap map/small's front map

                    if (!big.backOverlap.containsKey(maxBigBackOverlap)) {
                        big.backOverlap.put(maxBigBackOverlap, new HashSet<Read>());
                    }
                    if (!small.frontOverlap.containsKey(maxBigBackOverlap)) {
                        small.frontOverlap.put(maxBigBackOverlap, new HashSet<Read>());
                    }
                    big.backOverlap.get(maxBigBackOverlap).add(small);
                    small.frontOverlap.get(maxBigBackOverlap).add(big);

                    if (maxBigBackOverlap > totalMaxOverlap) {
                        totalMaxOverlap = maxBigBackOverlap;
                        totalFront = small;
                        totalBack = big;
                    }
                 }

                if (maxBigFrontOverlap > 0) {
                    // add it to big's frontoverlap map/big's back map

                    if (!big.frontOverlap.containsKey(maxBigFrontOverlap)) {
                        big.frontOverlap.put(maxBigFrontOverlap, new HashSet<Read>());
                    }
                    if (!small.backOverlap.containsKey(maxBigFrontOverlap)) {
                        small.backOverlap.put(maxBigFrontOverlap, new HashSet<Read>());
                    }
                    big.frontOverlap.get(maxBigFrontOverlap).add(small);
                    small.backOverlap.get(maxBigFrontOverlap).add(big);

                    if (maxBigFrontOverlap > totalMaxOverlap) {
                        totalMaxOverlap = maxBigFrontOverlap;
                        totalFront = big;
                        totalBack = small;
                    }
                }
            }
        }

        HashSet<Read> alreadyMerged = new HashSet<Read>();
        // start merging strings
        String finalString = null;

        // totalBack [xxxxxxxxxxxxAAAAA]
        // totalFront            [AAAAAxxxxxxxx]

        finalString = totalBack.read + totalFront.read.substring(totalMaxOverlap, totalFront.read.length());
        alreadyMerged.add(totalBack);
        alreadyMerged.add(totalFront);

        totalBack.backOverlap.get(totalMaxOverlap).remove(totalFront);
        if (totalBack.backOverlap.get(totalMaxOverlap).isEmpty()) {
            totalBack.backOverlap.remove(totalMaxOverlap);
        }
        totalFront.frontOverlap.get(totalMaxOverlap).remove(totalBack);
        if (totalFront.frontOverlap.get(totalMaxOverlap).isEmpty()) {
            totalFront.frontOverlap.remove(totalMaxOverlap);
        }

        int currentOverlap = totalMaxOverlap;
        Read currentEnd = totalFront;
        Read currentBegin = totalBack;

        // add the next read with the largest overlap
        while (alreadyMerged.size() < reads.size()) {
            int endMaxOverlap = 0;
            int beginMaxOverlap = 0;
            Read nextEnd = null;
            Read nextBegin = null;

            if (currentBegin.frontOverlap.isEmpty() && currentEnd.backOverlap.isEmpty()) {
                /* this occurs when adding the optimal next read to either end causes
                some read that was NOT optimal to not be able to be added anymore; should
                only occur in edge cases where reads with small overlaps (1-3 characters maybe)
                compete to be the next added read, and the highest overlap is coincidental */
                break;
            } else if (currentBegin.frontOverlap.isEmpty()) {
                beginMaxOverlap = 0;
                endMaxOverlap = Collections.max(currentEnd.backOverlap.keySet());
                nextBegin = new Read("");
                nextEnd = currentEnd.backOverlap.get(endMaxOverlap).iterator().next();
            } else if (currentEnd.backOverlap.isEmpty()) {
                beginMaxOverlap = Collections.max(currentBegin.frontOverlap.keySet());
                endMaxOverlap = 0;
                nextBegin = currentBegin.frontOverlap.get(beginMaxOverlap).iterator().next();
                nextEnd = new Read("");
            } else {
                endMaxOverlap = Collections.max(currentEnd.backOverlap.keySet());
                beginMaxOverlap = Collections.max(currentBegin.frontOverlap.keySet());
                nextEnd = currentEnd.backOverlap.get(endMaxOverlap).iterator().next();
                nextBegin = currentBegin.frontOverlap.get(beginMaxOverlap).iterator().next();
            }
            if (alreadyMerged.contains(nextEnd)) {
                currentEnd.backOverlap.get(endMaxOverlap).remove(nextEnd);
                if (currentEnd.backOverlap.get(endMaxOverlap).isEmpty()) {
                    currentEnd.backOverlap.remove(endMaxOverlap);
                }
                if (alreadyMerged.contains(nextBegin)) {
                    currentBegin.frontOverlap.get(beginMaxOverlap).remove(nextBegin);
                    if (currentBegin.frontOverlap.get(beginMaxOverlap).isEmpty()) {
                        currentBegin.frontOverlap.remove(beginMaxOverlap);
                    }
                }
            } else if (alreadyMerged.contains(nextBegin)) {
                currentBegin.frontOverlap.get(beginMaxOverlap).remove(nextBegin);
                if (currentBegin.frontOverlap.get(beginMaxOverlap).isEmpty()) {
                    currentBegin.frontOverlap.remove(beginMaxOverlap);
                }
            } else {
                if (endMaxOverlap == beginMaxOverlap) {
                    finalString = finalString + nextEnd.read.substring(endMaxOverlap, nextEnd.read.length());
                    finalString = nextBegin.read.substring(0, nextBegin.read.length()-beginMaxOverlap) + finalString;
                    alreadyMerged.add(nextEnd);
                    alreadyMerged.add(nextBegin);
                    currentBegin = nextBegin;
                    currentEnd = nextEnd;
                } else if (endMaxOverlap > beginMaxOverlap) {
                    finalString = finalString + nextEnd.read.substring(endMaxOverlap, nextEnd.read.length());
                    alreadyMerged.add(nextEnd);
                    currentEnd = nextEnd;
                } else if (endMaxOverlap < beginMaxOverlap) {
                    finalString = nextBegin.read.substring(0, nextBegin.read.length()-beginMaxOverlap) + finalString;
                    alreadyMerged.add(nextBegin);
                    currentBegin = nextBegin;
                }
            }
        }
        return finalString;
    }

    public static void main(String[] args) {
        // replace with directory containing read[#].txt files
        String readsDirectory = "C:\\Users\\Kevin\\Desktop\\CS 170\\Dataset\\";

        for (int i = 1; i <= 16; i++) {
            ArrayList<String> reads = new ArrayList<String>();
            String output = "";
            try {
                BufferedReader br = new BufferedReader(new FileReader(readsDirectory + "reads" + i + ".txt"));
                try {
                    String line = br.readLine();
                    while (line != null) {
                        reads.add(line);
                        line = br.readLine();
                    }
                } finally {
                    br.close();
                }
                output = algorithm(reads);
            } catch (Exception e) {
                System.out.println("Failed on reads #"+i+": "+e.getMessage());
            }
            FileWriter fileWriter = null;
            try {
                File newFile = new File(readsDirectory + "output" + i + ".txt");
                fileWriter = new FileWriter(newFile);
                fileWriter.write(output);
                fileWriter.close();
            } catch (Exception e) {
                System.out.println("Failed on reads #"+i+": "+e.getMessage());
            } finally {
                try {
                    fileWriter.close();
                } catch (Exception e) {
                    System.out.println("Failed on reads #"+i+": "+e.getMessage());
                }
            }
        }
    }
}
