/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */

package polishnlg.format.polish;

/**
 * This class keeps track of the prefix for numbered lists.
 */
public class NumberedPrefix {
    String prefix;

    public NumberedPrefix() {
        prefix = "0";
    }

    public void increment() {
        int dotPosition = prefix.lastIndexOf('.');
        if(dotPosition == -1) {
            int counter = Integer.valueOf(prefix);
            counter++;
            prefix = String.valueOf(counter);

        } else {
            final String subCounterStr = prefix.substring(dotPosition + 1);
            int subCounter = Integer.valueOf(subCounterStr);
            subCounter++;
            prefix = prefix.substring(0, dotPosition) + "." + String.valueOf(subCounter);
        }
    }

    /**
     * This method starts a new level to the prefix (e.g., 1.1 if the current is 1, 2.3.1 if current is 2.3, or 1 if the current is 0).
     */
    public void upALevel() {
        if(prefix.equals("0")) {
            prefix = "1";
        } else {
            prefix = prefix + ".1";
        }
    }

    /**
     * This method removes a level from the prefix (e.g., 0 if current is a plain number, say, 7, or 2.4, if current is 2.4.1).
     */
    public void downALevel() {
        int dotPosition = prefix.lastIndexOf('.');
        if(dotPosition == -1) {
            prefix = "0";
        } else {
            prefix = prefix.substring(0, dotPosition);
        }
    }

    public String getPrefix() {
        return prefix;
    }

    void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}