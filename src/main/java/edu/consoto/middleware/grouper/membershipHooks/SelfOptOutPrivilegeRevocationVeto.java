package edu.consoto.middleware.grouper.membershipHooks;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.hooks.MembershipHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.consoto.middleware.grouper.GrouperUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelfOptOutPrivilegeRevocationVeto extends MembershipHooks {

    private final static Logger logger = LoggerFactory.getLogger(SelfOptOutPrivilegeRevocationVeto.class);

    private static final String OPTOUTS_FIELD_NAME = "optouts";

    /** OptOut Required Attribute DefName, if found enforce this hook */
    public static final String OPTOUT_REQUIRED_ATTRIBUTE_DEF_NAME = GrouperConfig.retrieveConfig().propertyValueStringRequired("nd.optOutRequired.attributeDefName");

    @Override
    public void membershipPreRemoveMember(final HooksContext hooksContext, final HooksMembershipChangeBean preDeleteMemberBean) {
        

        //sample fix to apply... uncomment for deletes to work
        /*
        //if we are in group delete, then allow
        if (Group.deleteOccuring()) {
            return;
        }
        */
        
        try {
            final Field field = preDeleteMemberBean.getMembership().getField();

            if (OPTOUTS_FIELD_NAME.equals(field.getName())) {
                final Group thisGroup = preDeleteMemberBean.getGroup();

                //since we have security on the type/attribute, we need to do this as root
                GrouperSession.callbackGrouperSession(
                        GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {

                    @Override
                    public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

                        if (GrouperUtils.processGroup(thisGroup, OPTOUT_REQUIRED_ATTRIBUTE_DEF_NAME)) {
                            Group membershipGroup = preDeleteMemberBean.getMember().toGroup();

                            if (thisGroup.getUuid().equals(membershipGroup.getUuid())) {
                                throw new HookVeto("self.optout.remove.veto", "Cannot remove self-assigned OptOut privilege.");
                            }
                        }
                        return null;
                    }
                });
            }
        } catch (GroupNotFoundException e) {
            logger.error("Member is not a Group. Moving on...: ", e);
        }
    }
}
