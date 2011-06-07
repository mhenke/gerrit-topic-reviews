// Copyright (C) 2008 The Android Open Source Project
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

package com.google.gerrit.reviewdb;

import com.google.gerrit.reviewdb.Change;
import com.google.gwtorm.client.Column;
import com.google.gwtorm.client.IntKey;

import java.sql.Timestamp;

/** A revision of a {@link Topic}. */
public final class ChangeSet {
  private static final String REFS_TOPICS = "refs/topics/";

  /** Is the reference name a topic reference? */
  // TODO
  // What kind of reference will have a topic?
  public static boolean isRef(final String name) {
    return name.matches("^refs/topics/.*/[1-9][0-9]*/[1-9][0-9]*$");
  }

  public static class Id extends IntKey<Change.Id> {
    private static final long serialVersionUID = 1L;

    @Column(id = 1)
    protected Change.Id topicId;

    @Column(id = 2)
    protected int changeSetId;

    protected Id() {
      topicId = new Change.Id();
    }

    public Id(final Change.Id topic, final int id) {
      this.topicId = topic;
      this.changeSetId = id;
    }

    @Override
    public Change.Id getParentKey() {
      return topicId;
    }

    public void setParentKey(final Change.Id id) {
      topicId = id;
    }

    @Override
    public int get() {
      return changeSetId;
    }

    @Override
    protected void set(int newValue) {
      changeSetId = newValue;
    }

    /** Parse a ChangeSet.Id out of a string representation. */
    public static Id parse(final String str) {
      final Id r = new Id();
      r.fromString(str);
      return r;
    }

    // TODO do we really need this?
    /** Parse a ChangeSet.Id from a {@link ChangeSet#getRefName()} result. */
    public static Id fromRef(String name) {
      if (!name.startsWith(REFS_TOPICS)) {
        throw new IllegalArgumentException("Not a ChangeSet.Id: " + name);
      }
      final String[] parts = name.substring(REFS_TOPICS.length()).split("/");
      final int n = parts.length;
      if (n < 2) {
        throw new IllegalArgumentException("Not a ChangeSet.Id: " + name);
      }
      // TODO
      // What if there are more than 2????
      final int topicId = Integer.parseInt(parts[n - 2]);
      final int changeSetId = Integer.parseInt(parts[n - 1]);
      return new ChangeSet.Id(new Change.Id(topicId), changeSetId);
    }
  }

  @Column(id = 1, name = Column.NONE)
  protected Id id;

  // TODO
//  @Column(id = 2, notNull = false)
//  protected Change.Id topicId;

  @Column(id = 2, name = "uploader_account_id")
  protected Account.Id uploader;

  /** When this patch set was first introduced onto the change. */
  @Column(id = 3)
  protected Timestamp createdOn;

  protected ChangeSet() {
  }

  public ChangeSet(final ChangeSet.Id k) {
    id = k;
  }

  public ChangeSet.Id getId() {
    return id;
  }

  public int getChangeSetId() {
    return id.get();
  }

  // TODO
  // Do we need the revision related data?
  public Change.Id getTopicId() {
    return id.getParentKey();
  }

  public void setTopicId(final Change.Id i) {
    id.setParentKey(i);
  }

  public Account.Id getUploader() {
    return uploader;
  }

  public void setUploader(final Account.Id who) {
    uploader = who;
  }

  public Timestamp getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(final Timestamp ts) {
    createdOn = ts;
  }

  // TODO
  // cowboy thing?
  // TODO
  // Whate we are doing here?
  public String getRefName() {
    final StringBuilder r = new StringBuilder();
    r.append(REFS_TOPICS);
    final int topicId = id.getParentKey().get();
    final int m = topicId % 100;
    if (m < 10) {
      r.append('0');
    }
    r.append(m);
    r.append('/');
    r.append(topicId);
    r.append('/');
    r.append(id.get());
    return r.toString();
  }

  @Override
  public String toString() {
    return "[ChangeSet " + getId().toString() + "]";
  }
}
