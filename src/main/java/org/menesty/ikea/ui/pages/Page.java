package org.menesty.ikea.ui.pages;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;

/**
 * User: Menesty
 * Date: 10/10/13
 * Time: 8:00 AM
 */
public abstract class Page extends TreeItem<String> {

    protected Page(String name) {
        super(name);
    }

    public void setName(String name){
        setValue(name);
    }

    public String getName() {
        return getValue();
    }

    public String getPath() {
        if (getParent() == null) {
            return getName();
        } else {
            String parentsPath = ((Page)getParent()).getPath();
            if (parentsPath.equalsIgnoreCase("All")) {
                return getName();
            } else {
                return  parentsPath + "/" + getName();
            }
        }
    }

    public abstract Node createView();

    /** find a child with a '/' deliminated path */
    public Page getChild(String path) {
//        System.out.println("Page.getChild("+path+")");
//        new Throwable().printStackTrace(System.out);
        int firstIndex = path.indexOf('/');
        String childName = (firstIndex==-1) ? path : path.substring(0,firstIndex);
        String anchor = null;
        if (childName.indexOf('#') != -1) {
            String[] parts = childName.split("#");
//            System.out.println("childName = " + childName);
            childName = parts[0];
//            System.out.println("childName AFTER = " + childName);
            anchor = (parts.length == 2) ? parts[1] : null;
//            System.out.println("anchor = " + anchor);
        }
//        System.out.println("childName = " + childName);
        for (TreeItem child : getChildren()) {
            Page page = (Page)child;
            if(page.getName().equals(childName)) {
                if(firstIndex==-1) {
                    if (page instanceof DocPage) {
                        ((DocPage)page).setAnchor(anchor);
                    }
                    return page;
                } else {
                    return page.getChild(path.substring(firstIndex+1));
                }
            }
        }
        return null;
    }

    @Override public String toString() {
        return toString("");
    }

    private String toString(String indent) {
        String out = indent + "["+getName()+"] "+getClass().getSimpleName();
        ObservableList<TreeItem<String>> childModel = getChildren();
        if (childModel!=null) {
            for (TreeItem child:childModel) {
                out += "\n"+((Page)child).toString("    "+indent);
            }
        }
        return out;
    }

    public static class GoToPageEventHandler implements EventHandler {
        private String pagePath;

        public GoToPageEventHandler(String pagePath) {
            this.pagePath = pagePath;
        }

        public void handle(Event event) {
            Ensemble2.getEnsemble2().goToPage(pagePath);
        }
    }
}