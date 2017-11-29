package com.zw.archer.comment.service;

import com.zw.archer.comment.model.Comment;
import com.zw.archer.node.model.Node;

public interface CommentService {
	/**
	 * 获取一个node的一级评论数量。
	 * 
	 * @param node
	 * @return
	 */
	public Long getCommentNumberFL(Node node);

	/**
	 * 获取一个comment的一级孩子数量。
	 * 
	 * @param node
	 * @return
	 */
	public Long getCommentNumberFC(Comment comment);

	public Comment findById(String commentId);
}
