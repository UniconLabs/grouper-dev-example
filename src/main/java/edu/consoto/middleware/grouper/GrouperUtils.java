package edu.consoto.middleware.grouper;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Stem;

/**
 * Created by jgasper on 9/1/15.
 */
public class GrouperUtils {
    /**
     *
     * @param group
     * @param attributeFlag
     * @return
     */
    public static boolean processGroup(Group group, String attributeFlag) {
        if (group == null) {
            return false;
        }

        return group.getAttributeDelegate().hasAttributeByName(attributeFlag) || processStem(group.getParentStem(), attributeFlag);
    }

    /**
     *
     * @param stem
     * @param attributeFlag
     * @return
     */
    public static boolean processStem(Stem stem, String attributeFlag) {
        if (stem == null || stem.isRootStem()) {
            return false;
        }

        return stem.getAttributeDelegate().hasAttributeByName(attributeFlag) || processStem(stem.getParentStem(), attributeFlag);
    }
}
