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

package com.google.gerrit.common.data;

import com.google.gerrit.reviewdb.ApprovalCategory;
import com.google.gerrit.reviewdb.ChangeSet;
import com.google.gerrit.reviewdb.Topic;
import com.google.gerrit.reviewdb.TopicMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/** Detail necessary to display a topic. */
public class TopicDetail {
  protected AccountInfoCache accounts;
  protected boolean allowsAnonymous;
  protected boolean canAbandon;
  protected boolean canRestore;
  protected boolean canRevert;
  protected Topic topic;
  protected boolean starred;
  protected List<ChangeInfo> dependsOn;
  protected List<ChangeInfo> neededBy;
  protected List<ChangeSet> changeSets;
  protected List<ApprovalDetail> approvals;
  protected Set<ApprovalCategory.Id> missingApprovals;
  protected boolean canSubmit;
  protected List<TopicMessage> messages;
  protected ChangeSet.Id currentChangeSetId;
  protected ChangeSetDetail currentDetail;

  public TopicDetail() {
  }

  public AccountInfoCache getAccounts() {
    return accounts;
  }

  public void setAccounts(AccountInfoCache aic) {
    accounts = aic;
  }

  public boolean isAllowsAnonymous() {
    return allowsAnonymous;
  }

  public void setAllowsAnonymous(final boolean anon) {
    allowsAnonymous = anon;
  }

  public boolean canAbandon() {
    return canAbandon;
  }

  public void setCanAbandon(final boolean a) {
    canAbandon = a;
  }

  public boolean canRestore() {
    return canRestore;
  }

  public void setCanRestore(final boolean a) {
    canRestore = a;
  }

  public boolean canRevert() {
    return canRevert;
  }

  public void setCanRevert(boolean a) {
      canRevert = a;
  }

  public boolean canSubmit() {
    return canSubmit;
  }

  public void setCanSubmit(boolean a) {
    canSubmit = a;
  }

  public Topic getTopic() {
    return topic;
  }

  public void setTopic(final Topic topic) {
    this.topic = topic;
    this.currentChangeSetId = topic.currentChangeSetId();
  }

  public boolean isStarred() {
    return starred;
  }

  public void setStarred(final boolean s) {
    starred = s;
  }

  public List<ChangeInfo> getDependsOn() {
    return dependsOn;
  }

  public void setDependsOn(List<ChangeInfo> d) {
    dependsOn = d;
  }

  public List<ChangeInfo> getNeededBy() {
    return neededBy;
  }

  public void setNeededBy(List<ChangeInfo> d) {
    neededBy = d;
  }

  public List<TopicMessage> getMessages() {
    return messages;
  }

  public void setMessages(List<TopicMessage> m) {
    messages = m;
  }

  public List<ChangeSet> getChangeSets() {
    return changeSets;
  }

  public void setChangeSets(List<ChangeSet> s) {
    changeSets = s;
  }

  public List<ApprovalDetail> getApprovals() {
    return approvals;
  }

  public void setApprovals(Collection<ApprovalDetail> list) {
    approvals = new ArrayList<ApprovalDetail>(list);
    Collections.sort(approvals, ApprovalDetail.SORT);
  }

  public Set<ApprovalCategory.Id> getMissingApprovals() {
    return missingApprovals;
  }

  public void setMissingApprovals(Set<ApprovalCategory.Id> a) {
    missingApprovals = a;
  }

  public boolean isCurrentChangeSet(final ChangeSetDetail detail) {
    return currentChangeSetId != null
        && detail.getChangeSet().getId().equals(currentChangeSetId);
  }

  public ChangeSet getCurrentChangeSet() {
    if (currentChangeSetId != null) {
      for (int i = changeSets.size() - 1; i >= 0; i--) {
        final ChangeSet cs = changeSets.get(i);
        if (cs.getId().equals(currentChangeSetId)) {
          return cs;
        }
      }
    }
    return null;
  }

  public ChangeSetDetail getCurrentChangeSetDetail() {
    return currentDetail;
  }

  public void setCurrentChangeSetDetail(ChangeSetDetail d) {
    currentDetail = d;
  }

  public String getDescription() {
    return currentDetail != null ? currentDetail.getInfo().getMessage() : "";
  }
}
