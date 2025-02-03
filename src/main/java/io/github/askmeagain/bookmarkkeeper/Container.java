package io.github.askmeagain.bookmarkkeeper;

import com.intellij.ide.bookmark.Bookmark;
import com.intellij.ide.bookmark.BookmarkType;

import java.util.List;

public class Container {
  final String groupName;
  final boolean isDefault;
  final List<BookmarkInfo> bookmarks;

  public Container(String groupName, boolean isDefault, List<BookmarkInfo> bookmarks) {
    this.groupName = groupName;
    this.isDefault = isDefault;
    this.bookmarks = bookmarks;
  }

  // Inner class to hold bookmark details
  public static class BookmarkInfo {
    final Bookmark bookmark;
    final BookmarkType type;

    public BookmarkInfo(Bookmark bookmark, BookmarkType type) {
      this.bookmark = bookmark;
      this.type = type;
    }
  }
}