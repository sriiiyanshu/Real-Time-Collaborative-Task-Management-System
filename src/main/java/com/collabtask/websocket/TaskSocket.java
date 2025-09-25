package com.collabtask.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/tasks/{projectId}")
public class TaskSocket {
	private static final ConcurrentHashMap<String, Set<Session>> projectSessions = new ConcurrentHashMap<>();

	@OnOpen
	public void onOpen(Session session, @PathParam("projectId") String projectId) {
		projectSessions.computeIfAbsent(projectId, k -> ConcurrentHashMap.newKeySet()).add(session);
	}

	@OnMessage
	public void onMessage(String message, Session session, @PathParam("projectId") String projectId) throws IOException {
		broadcast(projectId, message, session);
	}

	@OnClose
	public void onClose(Session session, @PathParam("projectId") String projectId) {
		Set<Session> set = projectSessions.get(projectId);
		if (set != null) set.remove(session);
	}

	@OnError
	public void onError(Session session, Throwable thr) { }

	public static void broadcast(String projectId, String message, Session exclude) throws IOException {
		Set<Session> set = projectSessions.get(projectId);
		if (set == null) return;
		for (Session s : set) {
			if (exclude != null && s.getId().equals(exclude.getId())) continue;
			s.getBasicRemote().sendText(message);
		}
	}
}
