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

import com.google.gerrit.client.Dispatcher;
import com.google.gerrit.client.FormatUtil;
import com.google.gerrit.client.Gerrit;
import com.google.gerrit.client.rpc.GerritCallback;
import com.google.gerrit.client.ui.AccountDashboardLink;
import com.google.gerrit.client.ui.ChangeLink;
import com.google.gerrit.client.ui.ComplexDisclosurePanel;
import com.google.gerrit.client.ui.ListenableAccountDiffPreference;
import com.google.gerrit.client.ui.NavigationTable;
import com.google.gerrit.common.data.ChangeDetail;
import com.google.gerrit.common.data.ChangeInfo;
import com.google.gerrit.common.data.ChangeSetDetail;
import com.google.gerrit.common.data.GitwebLink;
import com.google.gerrit.common.data.PatchSetDetail;
import com.google.gerrit.common.data.TopicDetail;
import com.google.gerrit.reviewdb.Account;
import com.google.gerrit.reviewdb.AccountDiffPreference;
import com.google.gerrit.reviewdb.AccountGeneralPreferences;
import com.google.gerrit.reviewdb.AccountGeneralPreferences.DownloadCommand;
import com.google.gerrit.reviewdb.AccountGeneralPreferences.DownloadScheme;
import com.google.gerrit.reviewdb.Branch;
import com.google.gerrit.reviewdb.Change;
import com.google.gerrit.reviewdb.ChangeMessage;
import com.google.gerrit.reviewdb.ChangeSet;
import com.google.gerrit.reviewdb.ChangeSetInfo;
import com.google.gerrit.reviewdb.Patch;
import com.google.gerrit.reviewdb.PatchSet;
import com.google.gerrit.reviewdb.PatchSetInfo;
import com.google.gerrit.reviewdb.Project;
import com.google.gerrit.reviewdb.TopicMessage;
import com.google.gerrit.reviewdb.UserIdentity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwtexpui.clippy.client.CopyableLabel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ChangeSetComplexDisclosurePanel extends ComplexDisclosurePanel implements OpenHandler<DisclosurePanel> {
  // TODO
  private static final int R_AUTHOR = 0;
  private static final int R_DOWNLOAD = 1;
  private static final int R_CNT = 2;

  private final TopicScreen topicScreen;
  private final TopicDetail topicDetail;
  private final ChangeSet changeSet;
  private final FlowPanel body;

  private Grid infoTable;
  private Panel actionsPanel;
  private ChangeTable changeTable;
  private final Set<ClickHandler> registeredClickHandler =  new HashSet<ClickHandler>();

  private PatchSet.Id diffBaseId;

  // TODO
  /**
   * Creates a closed complex disclosure panel for a change set.
   * The change set details are loaded when the complex disclosure panel is opened.
   */
  ChangeSetComplexDisclosurePanel(final TopicScreen parent, final TopicDetail detail,
      final ChangeSet cs) {
    this(parent, detail, cs, false);
    addOpenHandler(this);
  }

  // TODO
  /**
   * Creates an open complex disclosure panel for a patch set.
   */
  ChangeSetComplexDisclosurePanel(final TopicScreen parent, final TopicDetail detail,
      final ChangeSetDetail csd) {
    this(parent, detail, csd.getChangeSet(), true);
    ensureLoaded(csd);
  }

  // TODO
  private ChangeSetComplexDisclosurePanel(final TopicScreen parent, final TopicDetail detail,
      final ChangeSet cs, boolean isOpen) {
    super(Util.TM.changeSetHeader(cs.getChangeSetId()), isOpen);
    topicScreen = parent;
    topicDetail = detail;
    changeSet = cs;
    body = new FlowPanel();
    setContent(body);

    // TODO gitweb support?
//    final GitwebLink gw = Gerrit.getConfig().getGitwebLink();
//
//    // TODO
//    // It was asking for revision. is the changesetid or
//    // NOT NEEDED
//    final InlineLabel revtxt = new InlineLabel(cs.getId().get() + " ");
//    revtxt.addStyleName(Gerrit.RESOURCES.css().patchSetRevision());
//    getHeader().add(revtxt);
//    if (gw != null) {
//      final Anchor revlink =
//          new Anchor("(gitweb)", false, gw.toRevision(detail.getTopic()
//              .getProject(), cs));
//      revlink.addStyleName(Gerrit.RESOURCES.css().patchSetLink());
//      getHeader().add(revlink);
//    }
  }

  public void setDiffBaseId(PatchSet.Id diffBaseId) {
    this.diffBaseId = diffBaseId;
  }

  // TODO
  /**
   * Display the table showing the Author and Download links,
   * followed by the action buttons.
   */
  public void ensureLoaded(final ChangeSetDetail detail) {
    infoTable = new Grid(R_CNT, 2);
    infoTable.setStyleName(Gerrit.RESOURCES.css().infoBlock());
    // TODO
    // New style ??
    infoTable.addStyleName(Gerrit.RESOURCES.css().patchSetInfoBlock());

    initRow(R_AUTHOR, Util.C.patchSetInfoAuthor());
    initRow(R_DOWNLOAD, Util.C.patchSetInfoDownload());

    final CellFormatter itfmt = infoTable.getCellFormatter();
    itfmt.addStyleName(0, 0, Gerrit.RESOURCES.css().topmost());
    itfmt.addStyleName(0, 1, Gerrit.RESOURCES.css().topmost());
    itfmt.addStyleName(R_CNT - 1, 0, Gerrit.RESOURCES.css().bottomheader());
    itfmt.addStyleName(R_AUTHOR, 1, Gerrit.RESOURCES.css().useridentity());
    itfmt.addStyleName(R_DOWNLOAD, 1, Gerrit.RESOURCES.css()
        .downloadLinkListCell());

    final ChangeSetInfo info = detail.getInfo();
    displayUserIdentity(R_AUTHOR, info.getAuthor());
    // TODO
    // Not at the moment
//    displayDownload();

    body.add(infoTable);

    List<Change> currChanges = detail.getChanges();
    List<ChangeInfo> cil = new ArrayList<ChangeInfo>();

    for (Change ch : currChanges) cil.add(new ChangeInfo(ch));

    changeTable = new ChangeTable();
    ChangeTable.Section section = new ChangeTable.Section();
    if (!cil.isEmpty()) {
      changeTable.addSection(section);
      section.display(cil);
      changeTable.finishDisplay();
    }
    body.add(changeTable);

    if (!changeSet.getId().equals(diffBaseId)) {
      actionsPanel = new FlowPanel();
      actionsPanel.setStyleName(Gerrit.RESOURCES.css().patchSetActions());
      body.add(actionsPanel);
      if (Gerrit.isSignedIn()) {
        populateReviewAction();
        if (topicDetail.isCurrentChangeSet(detail)) {
          populateActions(detail);
        }
        populateDiffAllActions(detail);
      }
    }

    // TODO
    // We need to do this in a different way
    // What is this? the file list and the buttons??
//    if (!changeSet.getId().equals(diffBaseId)) {
//      patchTable = new PatchTable();
//      patchTable.setSavePointerId("PatchTable " + patchSet.getId());
//      patchTable.setPatchSetIdToCompareWith(diffBaseId);
//      patchTable.display(detail);
//
//      actionsPanel = new FlowPanel();
//      actionsPanel.setStyleName(Gerrit.RESOURCES.css().patchSetActions());
//      body.add(actionsPanel);
//      if (Gerrit.isSignedIn()) {
//        populateReviewAction();
//        // TODO
////        if (topicDetail.isCurrentPatchSet(detail)) {
////          populateActions(detail);
////        }
//      }
//      populateDiffAllActions(detail);
//      body.add(patchTable);
//
//      for(ClickHandler clickHandler : registeredClickHandler) {
//        patchTable.addClickHandler(clickHandler);
//      }
//    }
  }

  // TODO
  private void displayDownload() {
    final Project.NameKey projectKey = topicDetail.getTopic().getProject();
    final String projectName = projectKey.get();
    final CopyableLabel copyLabel = new CopyableLabel("");
    final DownloadCommandPanel commands = new DownloadCommandPanel();
    final DownloadUrlPanel urls = new DownloadUrlPanel(commands);
    final Set<DownloadScheme> allowedSchemes = Gerrit.getConfig().getDownloadSchemes();

    copyLabel.setStyleName(Gerrit.RESOURCES.css().downloadLinkCopyLabel());

    if (topicDetail.isAllowsAnonymous()
        && Gerrit.getConfig().getGitDaemonUrl() != null
        && (allowedSchemes.contains(DownloadScheme.ANON_GIT) ||
            allowedSchemes.contains(DownloadScheme.DEFAULT_DOWNLOADS))) {
      StringBuilder r = new StringBuilder();
      r.append(Gerrit.getConfig().getGitDaemonUrl());
      r.append(projectName);
      r.append(" ");
      r.append(changeSet.getRefName());
      urls.add(new DownloadUrlLink(DownloadScheme.ANON_GIT, Util.M
          .anonymousDownload("Git"), r.toString()));
    }

    if (topicDetail.isAllowsAnonymous()
        && (allowedSchemes.contains(DownloadScheme.ANON_HTTP) ||
            allowedSchemes.contains(DownloadScheme.DEFAULT_DOWNLOADS))) {
      StringBuilder r = new StringBuilder();
      r.append(GWT.getHostPageBaseURL());
      r.append("p/");
      r.append(projectName);
      r.append(" ");
      r.append(changeSet.getRefName());
      urls.add(new DownloadUrlLink(DownloadScheme.ANON_HTTP, Util.M
          .anonymousDownload("HTTP"), r.toString()));
    }

    if (Gerrit.getConfig().getSshdAddress() != null && Gerrit.isSignedIn()
        && Gerrit.getUserAccount().getUserName() != null
        && Gerrit.getUserAccount().getUserName().length() > 0
        && (allowedSchemes.contains(DownloadScheme.SSH) ||
            allowedSchemes.contains(DownloadScheme.DEFAULT_DOWNLOADS))) {
      String sshAddr = Gerrit.getConfig().getSshdAddress();
      final StringBuilder r = new StringBuilder();
      r.append("ssh://");
      r.append(Gerrit.getUserAccount().getUserName());
      r.append("@");
      if (sshAddr.startsWith("*:") || "".equals(sshAddr)) {
        r.append(Window.Location.getHostName());
      }
      if (sshAddr.startsWith("*")) {
        sshAddr = sshAddr.substring(1);
      }
      r.append(sshAddr);
      r.append("/");
      r.append(projectName);
      r.append(" ");
      r.append(changeSet.getRefName());
      urls.add(new DownloadUrlLink(DownloadScheme.SSH, "SSH", r.toString()));
    }

    if (Gerrit.isSignedIn() && Gerrit.getUserAccount().getUserName() != null
        && Gerrit.getUserAccount().getUserName().length() > 0
        && (allowedSchemes.contains(DownloadScheme.HTTP) ||
            allowedSchemes.contains(DownloadScheme.DEFAULT_DOWNLOADS))) {
      String base = GWT.getHostPageBaseURL();
      int p = base.indexOf("://");
      int s = base.indexOf('/', p + 3);
      if (s < 0) {
        s = base.length();
      }
      String host = base.substring(p + 3, s);
      if (host.contains("@")) {
        host = host.substring(host.indexOf('@') + 1);
      }

      final StringBuilder r = new StringBuilder();
      r.append(base.substring(0, p + 3));
      r.append(Gerrit.getUserAccount().getUserName());
      r.append('@');
      r.append(host);
      r.append(base.substring(s));
      r.append("p/");
      r.append(projectName);
      r.append(" ");
      r.append(changeSet.getRefName());
      urls.add(new DownloadUrlLink(DownloadScheme.HTTP, "HTTP", r.toString()));
    }

    if (allowedSchemes.contains(DownloadScheme.REPO_DOWNLOAD)) {
      // This site prefers usage of the 'repo' tool, so suggest
      // that for easy fetch.
      //
      final StringBuilder r = new StringBuilder();
      r.append("repo download ");
      r.append(projectName);
      r.append(" ");
      r.append(topicDetail.getTopic().getTopicId());
      r.append("/");
      r.append(changeSet.getChangeSetId());
      final String cmd = r.toString();
      commands.add(new DownloadCommandLink(DownloadCommand.REPO_DOWNLOAD,
          "repo download") {
        @Override
        void setCurrentUrl(DownloadUrlLink link) {
          urls.setVisible(false);
          copyLabel.setText(cmd);
        }
      });
    }

    if (!urls.isEmpty()) {
      commands.add(new DownloadCommandLink(DownloadCommand.CHECKOUT, "checkout") {
        @Override
        void setCurrentUrl(DownloadUrlLink link) {
          urls.setVisible(true);
          copyLabel.setText("git fetch " + link.urlData
              + " && git checkout FETCH_HEAD");
        }
      });
      commands.add(new DownloadCommandLink(DownloadCommand.PULL, "pull") {
        @Override
        void setCurrentUrl(DownloadUrlLink link) {
          urls.setVisible(true);
          copyLabel.setText("git pull " + link.urlData);
        }
      });
      commands.add(new DownloadCommandLink(DownloadCommand.CHERRY_PICK,
          "cherry-pick") {
        @Override
        void setCurrentUrl(DownloadUrlLink link) {
          urls.setVisible(true);
          copyLabel.setText("git fetch " + link.urlData
              + " && git cherry-pick FETCH_HEAD");
        }
      });
      commands.add(new DownloadCommandLink(DownloadCommand.FORMAT_PATCH,
          "patch") {
        @Override
        void setCurrentUrl(DownloadUrlLink link) {
          urls.setVisible(true);
          copyLabel.setText("git fetch " + link.urlData
              + " && git format-patch -1 --stdout FETCH_HEAD");
        }
      });
    }

    final FlowPanel fp = new FlowPanel();
    if (!commands.isEmpty()) {
      final AccountGeneralPreferences pref;
      if (Gerrit.isSignedIn()) {
        pref = Gerrit.getUserAccount().getGeneralPreferences();
      } else {
        pref = new AccountGeneralPreferences();
        pref.resetToDefaults();
      }
      commands.select(pref.getDownloadCommand());
      urls.select(pref.getDownloadUrl());

      FlowPanel p = new FlowPanel();
      p.setStyleName(Gerrit.RESOURCES.css().downloadLinkHeader());
      p.add(commands);
      final InlineLabel glue = new InlineLabel();
      glue.setStyleName(Gerrit.RESOURCES.css().downloadLinkHeaderGap());
      p.add(glue);
      p.add(urls);

      fp.add(p);
      fp.add(copyLabel);
    }
    infoTable.setWidget(R_DOWNLOAD, 1, fp);
  }

  private void displayUserIdentity(final int row, final UserIdentity who) {
    if (who == null) {
      infoTable.clearCell(row, 1);
      return;
    }

    final FlowPanel fp = new FlowPanel();
    fp.setStyleName(Gerrit.RESOURCES.css().patchSetUserIdentity());
    if (who.getName() != null) {
      final Account.Id aId = who.getAccount();
      if (aId != null) {
        fp.add(new AccountDashboardLink(who.getName(), aId));
      } else {
        final InlineLabel lbl = new InlineLabel(who.getName());
        lbl.setStyleName(Gerrit.RESOURCES.css().accountName());
        fp.add(lbl);
      }
    }
    if (who.getEmail() != null) {
      fp.add(new InlineLabel("<" + who.getEmail() + ">"));
    }
    if (who.getDate() != null) {
      fp.add(new InlineLabel(FormatUtil.mediumFormat(who.getDate())));
    }
    infoTable.setWidget(row, 1, fp);
  }

  private void populateActions(final ChangeSetDetail detail) {
    final boolean isOpen = topicDetail.getTopic().getStatus().isOpen();

    if (isOpen && topicDetail.canSubmit()) {
      final Button b =
          new Button(Util.M
              .submitPatchSet(detail.getChangeSet().getChangeSetId()));
      b.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(final ClickEvent event) {
          // TODO
//          b.setEnabled(false);
//          Util.MANAGE_SVC.submit(patchSet.getId(),
//              new GerritCallback<ChangeDetail>() {
//                public void onSuccess(ChangeDetail result) {
//                  onSubmitResult(result);
//                }
//
//                @Override
//                public void onFailure(Throwable caught) {
//                  b.setEnabled(true);
//                  super.onFailure(caught);
//                }
//              });
        }
      });
      actionsPanel.add(b);
    }

    if (topicDetail.canRevert()) {
      final Button b = new Button(Util.C.buttonRevertChangeBegin());
      b.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(final ClickEvent event) {
          // TODO
//          b.setEnabled(false);
//          new CommentedChangeActionDialog(patchSet.getId(), createCommentedCallback(b),
//              Util.C.revertChangeTitle(), Util.C.headingRevertMessage(),
//              Util.C.buttonRevertChangeSend(), Util.C.buttonRevertChangeCancel(),
//              Gerrit.RESOURCES.css().revertChangeDialog(), Gerrit.RESOURCES.css().revertMessage(),
//              Util.M.revertChangeDefaultMessage(detail.getInfo().getSubject(), detail.getPatchSet().getRevision().get())) {
//                public void onSend() {
//                  Util.MANAGE_SVC.revertChange(getPatchSetId() , getMessageText(), createCallback());
//                }
//              }.center();
        }
      });
      actionsPanel.add(b);
    }

    if (topicDetail.canAbandon()) {
      final Button b = new Button(Util.C.buttonAbandonChangeBegin());
      b.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(final ClickEvent event) {
          // TODO
//          b.setEnabled(false);
//          new CommentedChangeActionDialog(patchSet.getId(), createCommentedCallback(b),
//              Util.C.abandonChangeTitle(), Util.C.headingAbandonMessage(),
//              Util.C.buttonAbandonChangeSend(), Util.C.buttonAbandonChangeCancel(),
//              Gerrit.RESOURCES.css().abandonChangeDialog(), Gerrit.RESOURCES.css().abandonMessage()) {
//                public void onSend() {
//                  Util.MANAGE_SVC.abandonChange(getPatchSetId() , getMessageText(), createCallback());
//                }
//              }.center();
        }
      });
      actionsPanel.add(b);
    }

    if (topicDetail.canRestore()) {
      final Button b = new Button(Util.C.buttonRestoreChangeBegin());
      b.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(final ClickEvent event) {
          // TODO
//          b.setEnabled(false);
//          new CommentedChangeActionDialog(patchSet.getId(), createCommentedCallback(b),
//              Util.C.restoreChangeTitle(), Util.C.headingRestoreMessage(),
//              Util.C.buttonRestoreChangeSend(), Util.C.buttonRestoreChangeCancel(),
//              Gerrit.RESOURCES.css().abandonChangeDialog(), Gerrit.RESOURCES.css().abandonMessage()) {
//                public void onSend() {
//                  Util.MANAGE_SVC.restoreChange(getPatchSetId(), getMessageText(), createCallback());
//                }
//              }.center();
        }
      });
      actionsPanel.add(b);
    }
  }

  private void populateDiffAllActions(final ChangeSetDetail detail) {
    final Button diffAllSideBySide = new Button(Util.C.buttonDiffAllSideBySide());
    diffAllSideBySide.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        for (Change c : detail.getChanges()) {
          // TODO
//          Window.open(Window.Location.getPath() + "#"
//              + Dispatcher.toPatchSideBySide(c.getKey()), "_blank", null);
        }
      }
    });
    actionsPanel.add(diffAllSideBySide);

    final Button diffAllUnified = new Button(Util.C.buttonDiffAllUnified());
    diffAllUnified.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        for (Change c : detail.getChanges()) {
          // TODO
//          Window.open(Window.Location.getPath() + "#"
//              + Dispatcher.toPatchUnified(c.getKey()), "_blank", null);
        }
      }
    });
    actionsPanel.add(diffAllUnified);
  }

  private void populateReviewAction() {
    final Button b = new Button(Util.C.buttonReview());
    // TODO
    b.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(final ClickEvent event) {
        Gerrit.display("topic,publish," + changeSet.getId().toString(),
            new PublishTopicCommentScreen(changeSet.getId()));
      }
    });
    actionsPanel.add(b);
  }

  public void refresh() {
    AccountDiffPreference diffPrefs;
    if (changeTable == null) {
      diffPrefs = new ListenableAccountDiffPreference().get();
    } else {
      // TODO
//      diffPrefs = changeTable.getPreferences().get();
    }

    // TODO
//    Util.DETAIL_SVC.patchSetDetail(patchSet.getId(), diffBaseId, diffPrefs,
//        new GerritCallback<PatchSetDetail>() {
//          @Override
//          public void onSuccess(PatchSetDetail result) {
//
//            if (patchSet.getId().equals(diffBaseId)) {
//              patchTable.setVisible(false);
//              actionsPanel.setVisible(false);
//            } else {
//
//              if (patchTable != null) {
//                patchTable.removeFromParent();
//              }
//              patchTable = new PatchTable();
//              patchTable.setPatchSetIdToCompareWith(diffBaseId);
//              patchTable.display(result);
//              body.add(patchTable);
//
//              for (ClickHandler clickHandler : registeredClickHandler) {
//                patchTable.addClickHandler(clickHandler);
//              }
//            }
//          }
//        });
  }

  @Override
  public void onOpen(final OpenEvent<DisclosurePanel> event) {
    if (infoTable == null) {
      AccountDiffPreference diffPrefs;
      if (diffBaseId == null) {
        diffPrefs = null;
      } else {
        diffPrefs = new ListenableAccountDiffPreference().get();
      }

      // TODO
//      Util.DETAIL_SVC.patchSetDetail(patchSet.getId(), diffBaseId, diffPrefs,
//          new GerritCallback<PatchSetDetail>() {
//            public void onSuccess(final PatchSetDetail result) {
//              ensureLoaded(result);
//              patchTable.setRegisterKeys(true);
//            }
//          });
    }
  }

  private void initRow(final int row, final String name) {
    infoTable.setText(row, 0, name);
    infoTable.getCellFormatter().addStyleName(row, 0,
        Gerrit.RESOURCES.css().header());
  }

  private void onSubmitResult(final TopicDetail result) {
    if (result.getTopic().getStatus() == Change.Status.NEW) {
      // The submit failed. Try to locate the message and display
      // it to the user, it should be the last one created by Gerrit.
      //
      TopicMessage msg = null;
      if (result.getMessages() != null && result.getMessages().size() > 0) {
        for (int i = result.getMessages().size() - 1; i >= 0; i--) {
          if (result.getMessages().get(i).getAuthor() == null) {
            msg = result.getMessages().get(i);
            break;
          }
        }
      }

      if (msg != null) {
        // TODO add Topic support in the SubmitFailureDialog
        //new SubmitFailureDialog(result, msg).center();
      }
    }
    // TODO
    topicScreen.update(result);
  }

  public ChangeSet getChangeSet() {
    return changeSet;
  }

  /**
   * Adds a click handler to the change table.
   * If the patch table is not yet initialized it is guaranteed that the click handler
   * is added to the change table after initialization.
   */
  public void addClickHandler(final ClickHandler clickHandler) {
    registeredClickHandler.add(clickHandler);
    // TODO
//    if (changeTable != null) {
//      changeTable.addClickHandler(clickHandler);
//    }
  }

  /** Activates / Deactivates the key navigation and the highlighting of the current row for the patch table */
  public void setActive(boolean active) {
    // TODO
//    if (patchTable != null) {
//      patchTable.setActive(active);
//    }
  }

  private AsyncCallback<ChangeDetail> createCommentedCallback(final Button b) {
    return new AsyncCallback<ChangeDetail>() {
      public void onSuccess(ChangeDetail result) {
        // TODO Add Topic support
//        changeScreen.update(result);
      }

      public void onFailure(Throwable caught) {
        b.setEnabled(true);
      }
    };
  }
}
