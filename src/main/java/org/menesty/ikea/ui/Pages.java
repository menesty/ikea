package org.menesty.ikea.ui;

import org.menesty.ikea.ui.pages.AllPagesPage;
import org.menesty.ikea.ui.pages.CategoryPage;
import org.menesty.ikea.ui.pages.Page;

/**
 * User: Menesty
 * Date: 10/10/13
 * Time: 7:56 AM
 */
public class Pages {
    public static final String IKEA = "IKEA";
    private AllPagesPage root;
    private CategoryPage ikeaCategory;

    public Pages() {
        // create all the pages
        root = new AllPagesPage();
        ikeaCategory = new CategoryPage(IKEA);
        root.getChildren().add(ikeaCategory);
    }

    public void parseSamples(){
        SampleHelper.getSamples(samples);
        // ADD PAGES TO HIGHLIGHTS CATEGORY
        highlightedSamples.getChildren().addAll(
                new SamplePage((SamplePage)getPage("SAMPLES/Web/Web View")),
                new SamplePage((SamplePage)getPage("SAMPLES/Web/H T M L Editor")),
                new SamplePage((SamplePage)getPage("SAMPLES/Graphics 3d/Cube")),
                new SamplePage((SamplePage)getPage("SAMPLES/Graphics 3d/Cube System")),
                new SamplePage((SamplePage)getPage("SAMPLES/Graphics 3d/Xylophone")),
                new SamplePage((SamplePage)getPage("SAMPLES/Media/Advanced Media")),
                new SamplePage((SamplePage)getPage("SAMPLES/Graphics/Digital Clock")),
                new SamplePage((SamplePage)getPage("SAMPLES/Graphics/Display Shelf")),
                new SamplePage((SamplePage)getPage("SAMPLES/Charts/Area/Adv Area Audio Chart")),
                new SamplePage((SamplePage)getPage("SAMPLES/Charts/Bar/Adv Bar Audio Chart")),
                new SamplePage((SamplePage)getPage("SAMPLES/Charts/Line/Advanced Stock Line Chart")),
                new SamplePage((SamplePage)getPage("SAMPLES/Charts/Custom/Adv Candle Stick Chart")),
                new SamplePage((SamplePage)getPage("SAMPLES/Charts/Scatter/Advanced Scatter Chart"))
        );
        // ADD PAGES TO NEW CATEGORY
        newSamples.getChildren().addAll(
                new SamplePage((SamplePage)getPage("SAMPLES/Canvas/Fireworks")),
                new SamplePage((SamplePage)getPage("SAMPLES/Controls/Pagination")),
                new SamplePage((SamplePage)getPage("SAMPLES/Controls/Color Picker")),
                new SamplePage((SamplePage)getPage("SAMPLES/Controls/List/List View Cell Factory")),
                new SamplePage((SamplePage)getPage("SAMPLES/Controls/Table/Table Cell Factory")),
                new SamplePage((SamplePage)getPage("SAMPLES/Graphics/Images/Image Operator")),
                new SamplePage((SamplePage)getPage("SAMPLES/Scenegraph/Events/Multi Touch"))
        );
    }

    public Page getPage(String name) {
        Page page = root.getChild(name);
//        if (page == null) {
//            System.err.print("Can not load page named '" + name + "'");
//        }
        return page;
    }


    public Page getRoot() {
        return root;
    }
}
