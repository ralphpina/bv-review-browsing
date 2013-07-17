package com.bazaarvoice.example.bvreviewbrowsing;


public class BVProductTree {
	
	public static final String TAG = "BVProductTree";
	
	private static BVProductTree productTree;
	private BVNode root;
	private BVNode currentNode;
	private int treeLevel;
	
	
	public static BVProductTree getInstance() {
		if (productTree == null) {
			productTree = new BVProductTree();
		}
		
		return productTree;
	}
	private BVProductTree() {
		setRoot(null);
		setCurrentNode(null);
		treeLevel = 0;
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
	
	public int getTreeLevel() {
		return treeLevel;
	}
	
	public void incrementTreeLevel() {
		treeLevel++;
	}
	
	public void decrementTreeLevel() {
		treeLevel--;
	}

}
