package com.bazaarvoice.example.bvreviewbrowsing;

import java.util.ArrayList;

import org.json.JSONObject;
import com.bazaarvoice.types.RequestType;

public class BVNode {
	
	public static final String TAG = "Node";
	
	private JSONObject data;
	private BVNode parent;
	private ArrayList<BVNode> children;
	private RequestType typeForNode;
	private RequestType typeForChildren;
	private boolean isRoot;
	private int nodeLevel;
	
	public BVNode() {
		setData(null);
		setParent(null);
		children = new ArrayList<BVNode>();
		setTypeForNode(null);
		setTypeForChildren(null);
		setIsRoot(false);
		setNodeLevel(0);
		
	}
	
	public BVNode(JSONObject data, BVNode parent, RequestType typeForNode) {
		setData(data);
		setParent(parent);
		setTypeForNode(typeForNode);
		children = new ArrayList<BVNode>();
		setTypeForChildren(null);
		setIsRoot(false);
		setNodeLevel(parent.getNodeLevel() + 1);
		
	}
	
	public BVNode(BVProductTree root, RequestType typeForNode, boolean isRoot) {
		setData(null);
		root.setRoot(this);
		setTypeForNode(typeForNode);
		children = new ArrayList<BVNode>();
		setTypeForChildren(null);
		setIsRoot(isRoot);
		setNodeLevel(0);
	}
	
	
	public ArrayList<BVNode> getChildren() {
		return children;
	}
	
	public void setChildren(ArrayList<BVNode> children) {
		this.children = children;
	}
	
	public void addChild(BVNode child) {
		children.add(child);
	}
	
	public void addChild(JSONObject data, RequestType typeForNode) {
		BVNode child = new BVNode(data, this, typeForNode);
		addChild(child);
	}

	public BVNode getParent() {
		return parent;
	}

	public void setParent(BVNode parent) {
		this.parent = parent;
	}

	public JSONObject getData() {
		return data;
	}

	public void setData(JSONObject data) {
		this.data = data;
	}
	
	public RequestType getTypeForChildren() {
		return typeForChildren;
	}

	public void setTypeForChildren(RequestType typeForChildren) {
		this.typeForChildren = typeForChildren;
	}

	public RequestType getTypeForNode() {
		return typeForNode;
	}

	public void setTypeForNode(RequestType typeForNode) {
		this.typeForNode = typeForNode;
	}

	public boolean isRoot() {
		return isRoot;
	}

	public void setIsRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	public int getNodeLevel() {
		return nodeLevel;
	}

	public void setNodeLevel(int nodeLevel) {
		this.nodeLevel = nodeLevel;
	}
	
}
