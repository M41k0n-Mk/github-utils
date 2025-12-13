CREATE TABLE IF NOT EXISTS lists (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS list_items (
  list_id TEXT NOT NULL,
  username TEXT NOT NULL,
  PRIMARY KEY (list_id, username),
  FOREIGN KEY (list_id) REFERENCES lists(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS history (
  id TEXT PRIMARY KEY,
  username TEXT NOT NULL,
  action TEXT NOT NULL CHECK(action IN ('follow','unfollow')),
  timestamp TEXT NOT NULL,
  source_list_id TEXT NULL,
  dry_run INTEGER NOT NULL DEFAULT 0,
  FOREIGN KEY (source_list_id) REFERENCES lists(id)
);
