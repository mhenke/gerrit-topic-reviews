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

package com.google.gerrit.client.ui;

import com.google.gerrit.reviewdb.Change;
import com.google.gerrit.reviewdb.Change.Id;
import com.google.gerrit.reviewdb.Project;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;

/** Link to the open changes of a project. */
public class BranchTopicLink extends Composite {
  private final HorizontalPanel hp;
  private final BranchLink bl;
  private final TopicLink tl;

  public BranchTopicLink(Project.NameKey project, Change.Status status,
      String branch, String topic, Id topicId) {
    this(branch, project, status, branch, topic, topicId);
  }

  public BranchTopicLink(String text, Project.NameKey project, Change.Status status,
      String branch, String topic, Id topicId) {
    hp = new HorizontalPanel();

    bl = new BranchLink(text, project, status, branch, topic);
    if (topicId != null) tl = new TopicLink(" (" + topic + ")", topicId);
    else {
      // TODO Remove this when the server side is ready and simply do not add tl to the HorizontalPanel
      String topicString;
      if (topic == null) topicString = "";
      else topicString = " (" + topic + ")";

      tl = new TopicLink(topicString, new Change.Id(1));
    }

    hp.add(bl);
    hp.add(tl);
    initWidget(hp);
  }
}