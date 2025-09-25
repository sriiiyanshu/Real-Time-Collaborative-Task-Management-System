package com.collabtask.model;

import java.time.Instant;

public class Models {
	public static class User {
		public long id;
		public String email;
		public String name;
		public String passwordHash;
		public Instant createdAt;
	}
	public static class Project {
		public long id;
		public String name;
		public String description;
		public long ownerId;
		public Instant createdAt;
	}
	public static class Task {
		public long id;
		public long projectId;
		public String title;
		public String description;
		public String status; // TODO: could be enum
		public long assigneeId;
		public Instant dueDate;
		public Instant createdAt;
		public Instant updatedAt;
	}
}
