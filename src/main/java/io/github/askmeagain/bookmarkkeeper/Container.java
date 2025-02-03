package io.github.askmeagain.bookmarkkeeper;

import com.intellij.ide.bookmark.Bookmark;
import com.intellij.ide.bookmark.BookmarkType;

import java.util.List;

public class Container {
  final BookmarkType type;
  final Bookmark bookmark;
  final List<String> groupNames;

  Container(BookmarkType type, Bookmark bookmark, List<String> groupNames) {
    this.type = type;
    this.bookmark = bookmark;
    this.groupNames = groupNames;
  }
}
