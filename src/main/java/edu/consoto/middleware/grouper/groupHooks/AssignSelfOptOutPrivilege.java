package edu.consoto.middleware.grouper.groupHooks;

import edu.consoto.middleware.grouper.GrouperUtils;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AssignSelfOptOutPrivilege adds opt-out privilege for the newly created groups to self (this group's subject)
 */
public class AssignSelfOptOutPrivilege extends GroupHooks {

    private final static Logger logger = LoggerFactory.getLogger(AssignSelfOptOutPrivilege.class);

    /** OptOut Required Attribute DefName, if found enforce this hook */
    public static final String OPTOUT_REQUIRED_ATTRIBUTE_DEF_NAME = GrouperConfig.retrieveConfig().propertyValueStringRequired("nd.optOutRequired.attributeDefName");

    @Override
    public void groupPostCommitInsert(final HooksContext hooksContext, final HooksGroupBean postCommitInsertBean) {

        try {
            //since we have security on the attribute, we need to do this as root
            GrouperSession.callbackGrouperSession(
                    GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {

                @Override
                public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

                    final Group thisGroup = GroupFinder.findByUuid(grouperSession, postCommitInsertBean.getGroup().getId(), false);
                    logger.debug("The Group: {}", thisGroup);
                    logger.debug("Group's subject {} ", thisGroup.toSubject());

                    if (GrouperUtils.processGroup(thisGroup, OPTOUT_REQUIRED_ATTRIBUTE_DEF_NAME)) {
                        //Set of priv flags. That one flag set represents 'opt out' priv
                        thisGroup.addMember(thisGroup.toSubject(), false, false, false,
                                false, false, false, false, true, false,
                                false, null, null, false);
                    }
                    
                    return null;
                }
            });
        }
        catch (Throwable e) {
            logger.error("OPT OUT Hook error: ", e);
        }
    }
}
