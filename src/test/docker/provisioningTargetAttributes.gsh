grouperSession = GrouperSession.startRootSession();

addStem("etc:attribute", "custom", "Custom")
attributeStem = StemFinder.findByName(GrouperSession.staticGrouperSession(), "etc:attribute:custom", true);
attrDef = attributeStem.addChildAttributeDef("OptOutRequiredDef", AttributeDefType.attr);
attrDef.setAssignToGroup(true);
attrDef.setAssignToStem(true);
attrDef.setValueType(AttributeDefValueType.string);
attrDef.store();

attrDefName = attributeStem.addChildAttributeDefName(attrDef,  "optOutRequired", "Opt Out Required");
attrDefName.setDescription("Any value will indicate that OptOut will be enforced.");
attrDefName.store();


addRootStem("test", "test");
stem = addStem("test", "mailingLists", "Mailing Lists");
stem.getAttributeValueDelegate().assignValue("etc:attribute:custom:optOutRequired", "enforce");


addStem("test", "regularGroups", "Regular Groups");

