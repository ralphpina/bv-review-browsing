package com.bazaarvoice.example.bvreviewbrowsing;


public class BVProductTree {
	
	public static final String TAG = "BVProductTree";
	
	private BVNode root;
	private BVNode currentNode;
	
	public BVProductTree() {
		setRoot(null);
		setCurrentNode(null);
	}
	
	public BVProductTree(BVNode root) {
		setRoot(root);
	}
		
	public BVNode getRoot() {
		return root;
	}

	public void setRoot(BVNode root) {
		this.root = root;
	}

	public BVNode getCurrentNode() {
		return currentNode;
	}

	public void setCurrentNode(BVNode currentNode) {
		this.currentNode = currentNode;
	}

}
