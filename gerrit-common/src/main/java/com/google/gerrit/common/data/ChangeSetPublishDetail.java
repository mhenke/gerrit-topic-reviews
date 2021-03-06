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
import com.google.gerrit.reviewdb.ChangeSetInfo;
import com.google.gerrit.reviewdb.PatchLineComment;
import com.google.gerrit.reviewdb.PatchSetApproval;
import com.google.gerrit.reviewdb.Topic;

import java.util.List;

public class ChangeSetPublishDetail {
  protected AccountInfoCache accounts;
  protected ChangeSetInfo changeSetInfo;
  protected Topic topic;
  // TODO What do we need from these?
  protected List<PatchLineComment> drafts;
  protected List<PermissionRange> labels;
  protected List<PatchSetApproval> given;
  protected boolean canSubmit;

  public List<PermissionRange> getLabels() {
    return labels;
  }

  public void setLabels(List<PermissionRange> labels) {
    this.labels = labels;
  }

  public List<PatchSetApproval> getGiven() {
    return given;
  }

  public void setGiven(List<PatchSetApproval> given) {
    this.given = given;
  }

  public void setAccounts(AccountInfoCache accounts) {
    this.accounts = accounts;
  }

  public void setChangeSetInfo(ChangeSetInfo changeSetInfo) {
    this.changeSetInfo = changeSetInfo;
  }

  public void setTopic(Topic topic) {
    this.topic = topic;
  }

  public void setDrafts(List<PatchLineComment> drafts) {
    this.drafts = drafts;
  }

  public void setCanSubmit(boolean allowed) {
    canSubmit = allowed;
  }

  public AccountInfoCache getAccounts() {
    return accounts;
  }

  public Topic getTopic() {
    return topic;
  }

  public ChangeSetInfo getChangeSetInfo() {
    return changeSetInfo;
  }

  public List<PatchLineComment> getDrafts() {
    return drafts;
  }

  public PermissionRange getRange(final String permissionName) {
    for (PermissionRange s : labels) {
      if (s.getName().equals(permissionName)) {
        return s;
      }
    }
    return null;
  }

  // TODO
  public PatchSetApproval getChangeApproval(ApprovalCategory.Id id) {
    for (PatchSetApproval a : given) {
      if (a.getCategoryId().equals(id)) {
        return a;
      }
    }
    return null;
  }

  public boolean canSubmit() {
    return canSubmit;
  }
}
