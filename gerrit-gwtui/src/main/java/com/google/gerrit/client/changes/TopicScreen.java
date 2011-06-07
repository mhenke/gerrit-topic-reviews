// Copyright (C) 2011 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.client.changes;

import com.google.gerrit.client.Gerrit;
import com.google.gerrit.client.rpc.GerritCallback;
import com.google.gerrit.client.rpc.ScreenLoadCallback;
import com.google.gerrit.client.ui.CommentPanel;
import com.google.gerrit.client.ui.ExpandAllCommand;
import com.google.gerrit.client.ui.LinkMenuBar;
import com.google.gerrit.client.ui.NeedsSignInKeyCommand;
import com.google.gerrit.client.ui.Screen;
import com.google.gerrit.common.data.AccountInfo;
import com.google.gerrit.common.data.AccountInfoCache;
import com.google.gerrit.common.data.ApprovalDetail;
import com.google.gerrit.common.data.ChangeDetail;
import com.google.gerrit.common.data.ChangeInfo;
import com.google.gerrit.common.data.ChangeSetDetail;
import com.google.gerrit.common.data.ToggleStarRequest;
import com.google.gerrit.common.data.TopicDetail;
import com.google.gerrit.common.data.TopicInfo;
import com.google.gerrit.reviewdb.Account;
import com.google.gerrit.reviewdb.AccountAccess;
import com.google.gerrit.reviewdb.ApprovalCategory;
import com.google.gerrit.reviewdb.Branch;
import com.google.gerrit.reviewdb.Change;
import com.google.gerrit.reviewdb.ChangeMessage;
import com.google.gerrit.reviewdb.ChangeSet;
import com.google.gerrit.reviewdb.ChangeSetInfo;
import com.google.gerrit.reviewdb.PatchSet;
import com.google.gerrit.reviewdb.PatchSetInfo;
import com.google.gerrit.reviewdb.Project;
import com.google.gerrit.reviewdb.Topic;
import com.google.gerrit.reviewdb.TopicMessage;
import com.google.gerrit.reviewdb.UserIdentity;
import com.google.gerrit.reviewdb.Change.Status;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwtexpui.globalkey.client.GlobalKey;
import com.google.gwtexpui.globalkey.client.KeyCommand;
import com.google.gwtexpui.globalkey.client.KeyCommandSet;
import com.google.gwtjsonrpc.client.VoidResult;
import com.google.gwtorm.client.OrmException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class TopicScreen extends Screen {
  private final Change.Id topicId;
  private final ChangeSet.Id openChangeSetId;

  private TopicDescriptionBlock descriptionBlock;
  private ApprovalTable approvals;

  private IncludedInTable includedInTable;
  private DisclosurePanel includedInPanel;
  private DisclosurePanel dependenciesPanel;
  private ChangeTable dependencies;
  private ChangeTable.Section dependsOn;
  private ChangeTable.Section neededBy;

  private ChangeSetsBlock changeSetsBlock;

  private Panel comments;

  private KeyCommandSet keysNavigation;
  private KeyCommandSet keysAction;
  private HandlerRegistration regNavigation;
  private HandlerRegistration regAction;

  private Grid grid;
  private ListBox changesList;

  /**
   * The topic id for which the old version history is valid.
   */
  private static Change.Id currentTopicId;

  // TODO
  /**
   * Which patch set id is the diff base.
   */
  private static PatchSet.Id diffBaseId;

  public TopicScreen(final Change.Id toShow) {
    topicId = toShow;
    openChangeSetId = null;

    // If we have any diff stored, make sure they are applicable to the
    // current change, discard them otherwise.
    //
    if (currentTopicId != null && !currentTopicId.equals(toShow)) {
      diffBaseId = null;
    }
    currentTopicId = toShow;
  }

  public TopicScreen(final ChangeSet.Id toShow) {
    topicId = toShow.getParentKey();
    openChangeSetId = toShow;
  }

  public TopicScreen(final TopicInfo t) {
    this(t.getId());
  }

  @Override
  public void onSignOut() {
    super.onSignOut();
  }

  @Override
  protected void onLoad() {
    super.onLoad();
    refresh();
  }

  @Override
  protected void onUnload() {
    if (regNavigation != null) {
      regNavigation.removeHandler();
      regNavigation = null;
    }
    if (regAction != null) {
      regAction.removeHandler();
      regAction = null;
    }
    super.onUnload();
  }

  @Override
  public void registerKeys() {
    super.registerKeys();
    regNavigation = GlobalKey.add(this, keysNavigation);
    regAction = GlobalKey.add(this, keysAction);
    if (openChangeSetId != null) {
      changeSetsBlock.activate(openChangeSetId);
    }
  }

//  public void refresh() {
//    Util.DETAIL_SVC.changeDetail(changeId,
//        new ScreenLoadCallback<ChangeDetail>(this) {
//          @Override
//          protected void preDisplay(final ChangeDetail r) {
//            display(r);
//          }
//
//          @Override
//          protected void postDisplay() {
//            patchSetsBlock.setRegisterKeys(true);
//          }
//        });
//  }

  public void refresh() {
    // TODO Dummy function, remove me when the server side is ready
    Account.Id aId = new Account.Id(1000000);

    Topic.Key tKey = new Topic.Key("T1100011");
    Change.Id tId = new Change.Id(1100011);
    Topic topic = new Topic(tKey, tId, aId, null);
    currentTopicId = tId;

    List<ChangeSet> lcs = new ArrayList<ChangeSet>();
    ChangeSetDetail csDetail = new ChangeSetDetail();
    ChangeSet.Id csId = null;
    lcs.clear();

    UserIdentity uid = new UserIdentity();
    uid.setAccount(aId);
    uid.setEmail("my@email.com");
    uid.setName("Random Name");
    uid.setTimeZone(0);

    ChangeSetInfo csi = null;
    for (int id = 1; id < 6; id++) {
      csId = new ChangeSet.Id(currentTopicId, id);
      ChangeSet cs = new ChangeSet(csId);
      cs.setTopicId(tId);
      csDetail.setChangeSet(cs);
      csId = new ChangeSet.Id(currentTopicId, id);
      csi = new ChangeSetInfo(csId);
      csi.setSubject("This is the subject from the change set number " + id);
      csi.setAuthor(uid);
      csi.setMessage("ChangeSetInfo message");
      csDetail.setInfo(csi);
      lcs.add(cs);
    }

    List<Change> lc = new ArrayList<Change>();
    for (int id = 1; id < 6; id++) {
      Change c = new Change(new Change.Key("KeyID" + id), new Change.Id(id),
          aId, new Branch.NameKey(new Project.NameKey("ProjectNamekey"), "BranchNamekey"));
      c.setTopicId(tId);
      PatchSetInfo ps = new PatchSetInfo(new PatchSet.Id(c.getId(), id));
      ps.setSubject("This is the patchSet Subject");
      c.setCurrentPatchSet(ps);
      c.setStatus(Change.Status.NEW);
      lc.add(c);
    }
    csDetail.setChanges(lc);

    List<TopicMessage> ltm = new ArrayList<TopicMessage>();
    ltm.clear();
    for (int i = 0; i < 5; i++) {
      TopicMessage.Key k = new TopicMessage.Key(currentTopicId, "" + i);
      TopicMessage m = new TopicMessage(k, aId);
      m.setMessage("Hello, this is the dummy message number " + i);

      ltm.add(m);
    }



    AccountInfo ai = new AccountInfo(aId);
    ai.setPreferredEmail("my@email.com");
    List<AccountInfo> aiList = new ArrayList<AccountInfo>();
    aiList.add(ai);
    AccountInfoCache aic = new AccountInfoCache(aiList);

    topic.setCurrentChangeSet(csi);
    topic.setKey(tKey);
    topic.setTopic("TestTopic");
    topic.setStatus(Status.NEW);

    Set<ApprovalCategory.Id> aprovals = new HashSet<ApprovalCategory.Id>();
    ApprovalCategory category = new ApprovalCategory(new ApprovalCategory.Id("1"), "categoryName");
    aprovals.add(category.getId());

    Collection<ApprovalDetail> a = new ArrayList<ApprovalDetail>();
    ApprovalDetail ad = new ApprovalDetail(aId);
    a.add(ad);

    List<ChangeInfo> cil = new ArrayList<ChangeInfo>();
    for (Change c : lc) {
      ChangeInfo ci = new ChangeInfo(c);
      cil.add(ci);
    }

    // Dummy data for TopicDetail
    TopicDetail tDetail = new TopicDetail();

    tDetail.setChangeSets(lcs);
    tDetail.setCurrentChangeSetDetail(csDetail);
    tDetail.setMessages(ltm);
    tDetail.setApprovals(a);
    tDetail.setCanAbandon(true);
    tDetail.setAccounts(aic);
    tDetail.setTopic(topic);
    tDetail.setMissingApprovals(aprovals);
    tDetail.setDependsOn(cil);
    tDetail.setNeededBy(cil);

    final Screen screen = this;

    if (screen.isAttached()) {
      display(tDetail);
      screen.display();
      changeSetsBlock.setRegisterKeys(true);
    }
  }

  @Override
  protected void onInitUI() {
    super.onInitUI();
    addStyleName(Gerrit.RESOURCES.css().changeScreen());

    // TODO Set navigation keys
    keysNavigation = new KeyCommandSet(Gerrit.C.sectionNavigation());
    keysAction = new KeyCommandSet(Gerrit.C.sectionActions());
    keysNavigation.add(new UpToListKeyCommand(0, 'u', Util.C.upToChangeList()));
    keysNavigation.add(new ExpandCollapseDependencySectionKeyCommand(0, 'd', Util.C.expandCollapseDependencies()));

    if (Gerrit.isSignedIn()) {
      keysAction.add(new PublishCommentsKeyCommand(0, 'r', Util.C
          .keyPublishComments()));
    }

    descriptionBlock = new TopicDescriptionBlock();
    add(descriptionBlock);

    approvals = new ApprovalTable();
    add(approvals);

    // TODO needed new string constant?
    includedInPanel = new DisclosurePanel(Util.C.changeScreenIncludedIn());
    includedInTable = new IncludedInTable(topicId);

    includedInPanel.setContent(includedInTable);
    add(includedInPanel);

    dependencies = new ChangeTable() {
      {
        table.setWidth("auto");
      }
    };
    dependsOn = new ChangeTable.Section(Util.C.changeScreenDependsOn());
    neededBy = new ChangeTable.Section(Util.C.changeScreenNeededBy());
    dependencies.addSection(dependsOn);
    dependencies.addSection(neededBy);

    // TODO needed new string constant?
    dependenciesPanel = new DisclosurePanel(Util.C.changeScreenDependencies());
    dependenciesPanel.setContent(dependencies);
    add(dependenciesPanel);

    changesList = new ListBox();
    changesList.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        final int index = changesList.getSelectedIndex();
        final String selectedChangeSet = changesList.getValue(index);
        if (index == 0) {
          diffBaseId = null;
        } else {
          // TODO this is wrong
          diffBaseId = PatchSet.Id.parse(selectedChangeSet);
        }
        if (changeSetsBlock != null) {
          // TODO add support for diffBaseId selection
          //changeSetsBlock.refresh(diffBaseId);
        }
      }
    });
    changesList.addItem(Util.C.baseDiffItem());

    grid = new Grid(1, 2);
    grid.setStyleName(Gerrit.RESOURCES.css().selectPatchSetOldVersion());
    grid.setText(0, 0, Util.C.oldVersionHistory());
    grid.setWidget(0, 1, changesList);
    add(grid);

    changeSetsBlock = new ChangeSetsBlock(this);
    add(changeSetsBlock);

    comments = new FlowPanel();
    comments.setStyleName(Gerrit.RESOURCES.css().changeComments());
    add(comments);
  }

  private void displayTitle(final Topic.Key topicId, final String subject) {
    final StringBuilder titleBuf = new StringBuilder();
    if (LocaleInfo.getCurrentLocale().isRTL()) {
      if (subject != null) {
        titleBuf.append(subject);
        titleBuf.append(" :");
      }
      titleBuf.append(Util.TM.topicScreenTitleId(topicId.abbreviate()));
    } else {
      titleBuf.append(Util.TM.topicScreenTitleId(topicId.abbreviate()));
      if (subject != null) {
        titleBuf.append(": ");
        titleBuf.append(subject);
      }
    }
    setPageTitle(titleBuf.toString());
  }

  void update(final TopicDetail detail) {
    display(detail);
    changeSetsBlock.setRegisterKeys(true);
  }

  private void display(final TopicDetail detail) {
    displayTitle(detail.getTopic().getKey(), detail.getTopic().getSubject());

    if (Status.MERGED == detail.getTopic().getStatus()) {
      includedInPanel.setVisible(true);
      includedInPanel.addOpenHandler(includedInTable);
    } else {
      includedInPanel.setVisible(false);
    }

    dependencies.setAccountInfoCache(detail.getAccounts());
    approvals.setAccountInfoCache(detail.getAccounts());

    descriptionBlock.display(detail.getTopic(), detail.getAccounts());

    dependsOn.display(detail.getDependsOn());
    neededBy.display(detail.getNeededBy());

    approvals.display(detail.getTopic(), detail.getMissingApprovals(), detail.getApprovals());

    for (ChangeSet cId : detail.getChangeSets()) {
      if (changesList != null) {
        changesList.addItem(Util.TM.changeSetHeader(cId.getChangeSetId()), cId
            .getId().toString());
      }
    }

    if (diffBaseId != null && changesList != null) {
      changesList.setSelectedIndex(diffBaseId.get());
    }

    changeSetsBlock.display(detail, diffBaseId);
    addComments(detail);

    // If any dependency change is still open, show our dependency list.
    //
    boolean depsOpen = false;
    if (!detail.getTopic().getStatus().isClosed()
        && detail.getDependsOn() != null) {
      for (final ChangeInfo ti : detail.getDependsOn()) {
        if (ti.getStatus() != Change.Status.MERGED) {
          depsOpen = true;
          break;
        }
      }
    }

    dependenciesPanel.setOpen(depsOpen);
  }

  private void addComments(final TopicDetail detail) {
    comments.clear();

    final AccountInfoCache accts = detail.getAccounts();
    final List<TopicMessage> msgList = detail.getMessages();

    HorizontalPanel title = new HorizontalPanel();
    title.setWidth("100%");
    // TODO Do we need a new string constant?
    title.add(new Label(Util.C.changeScreenComments()));
    if (msgList.size() > 1) {
      title.add(messagesMenuBar());
    }
    title.setStyleName(Gerrit.RESOURCES.css().blockHeader());
    comments.add(title);

    final long AGE = 7 * 24 * 60 * 60 * 1000L;
    final Timestamp aged = new Timestamp(System.currentTimeMillis() - AGE);

    for (int i = 0; i < msgList.size(); i++) {
      final TopicMessage msg = msgList.get(i);

      final AccountInfo author;
      if (msg.getAuthor() != null) {
        author = accts.get(msg.getAuthor());
      } else {
        final Account gerrit = new Account(null);
        gerrit.setFullName(Util.C.messageNoAuthor());
        author = new AccountInfo(gerrit);
      }

      boolean isRecent;
      if (i == msgList.size() - 1) {
        isRecent = true;
      } else {
        // TODO Instead of opening messages by strict age, do it by "unread"?
        isRecent = msg.getWrittenOn().after(aged);
      }

      final CommentPanel cp =
          new CommentPanel(author, msg.getWrittenOn(), msg.getMessage());
      cp.setRecent(isRecent);
      cp.addStyleName(Gerrit.RESOURCES.css().commentPanelBorder());
      if (i == msgList.size() - 1) {
        cp.addStyleName(Gerrit.RESOURCES.css().commentPanelLast());
        cp.setOpen(true);
      }
      comments.add(cp);
    }

    comments.setVisible(msgList.size() > 0);
  }

  private LinkMenuBar messagesMenuBar() {
    final Panel c = comments;
    final LinkMenuBar menuBar = new LinkMenuBar();
    menuBar.addItem(Util.C.messageExpandRecent(), new ExpandAllCommand(c, true) {
      @Override
      protected void expand(final CommentPanel w) {
        w.setOpen(w.isRecent());
      }
    });
    menuBar.addItem(Util.C.messageExpandAll(), new ExpandAllCommand(c, true));
    menuBar.addItem(Util.C.messageCollapseAll(), new ExpandAllCommand(c, false));
    menuBar.addStyleName(Gerrit.RESOURCES.css().commentPanelMenuBar());
    return menuBar;
  }

  public class UpToListKeyCommand extends KeyCommand {
    public UpToListKeyCommand(int mask, char key, String help) {
      super(mask, key, help);
    }

    @Override
    public void onKeyPress(final KeyPressEvent event) {
      Gerrit.displayLastChangeList();
    }
  }

  public class ExpandCollapseDependencySectionKeyCommand extends KeyCommand {
    public ExpandCollapseDependencySectionKeyCommand(int mask, char key, String help) {
      super(mask, key, help);
    }

    @Override
    public void onKeyPress(KeyPressEvent event) {
      dependenciesPanel.setOpen(!dependenciesPanel.isOpen());
    }
  }

  public class PublishCommentsKeyCommand extends NeedsSignInKeyCommand {
    public PublishCommentsKeyCommand(int mask, char key, String help) {
      super(mask, key, help);
    }

    @Override
    public void onKeyPress(final KeyPressEvent event) {
      ChangeSet.Id currentChangeSetId = changeSetsBlock.getCurrentChangeSetId();
      Gerrit.display("change,publish," + currentChangeSetId.toString(),
          new PublishTopicCommentScreen(currentChangeSetId));
    }
  }
}
