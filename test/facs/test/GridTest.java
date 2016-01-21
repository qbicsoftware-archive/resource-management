package facs.test;

import java.util.Random;

import com.google.gwt.dev.json.JsonValue;
import com.vaadin.client.ui.FontIcon;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.NumberRenderer;
import com.vaadin.ui.renderers.ProgressBarRenderer;


public class GridTest {
		

	public static class BoldLastNameRenderer extends HtmlRenderer {
		 
        @Override
        public elemental.json.JsonValue encode(String value) {
            int lastSpace = value.lastIndexOf(' ');
            if (lastSpace >= 0) {
                value = String
                        .format("<span style=\"pointer-events: none;\">%s<b>%s</b></span>",
                                value.substring(0, lastSpace),
                                value.substring(lastSpace));
            }
            return super.encode(value);
        }
    }
 
    public GridRendererSample() {
 
        Grid grid = new Grid();
        grid.setSizeFull();
 
        grid.addColumn("index", Integer.class)
                .setRenderer(new NumberRenderer("%02d")).setHeaderCaption("##")
                .setExpandRatio(0);
 
        grid.getColumn("index").setWidth(50);
 
        grid.addColumn("name", String.class)
                .setRenderer(new BoldLastNameRenderer()).setExpandRatio(2);
 
        grid.addColumn("progress", Double.class)
                .setRenderer(new ProgressBarRenderer()).setExpandRatio(2);
 
        grid.addColumn("weight", double[].class)
                .setRenderer(new SparklineRenderer()).setExpandRatio(4);
 
        grid.addColumn("edit", FontIcon.class).setWidth(35)
                .setRenderer(new FontIconRenderer(new RendererClickListener() {
                    @Override
                    public void click(RendererClickEvent e) {
                        Notification.show("Editing item " + e.getItemId());
                    }
                }));
 
        grid.addColumn("delete", FontIcon.class).setWidth(35)
                .setRenderer(new FontIconRenderer(new RendererClickListener() {
                    @Override
                    public void click(RendererClickEvent e) {
                        Notification.show("Deleted item " + e.getItemId());
                    }
                }));
 
        grid.getDefaultHeaderRow().join("edit", "delete").setText("Tools");
 
        Random r = new Random();
        for (int i = 0; i < 100; ++i) {
            String[] name = ExampleUtil.getRandomName(r);
            grid.addRow(
            /* index */i,
            /* name */name[0] + ' ' + name[1],
            /* progress */Math.sin(i / 3.0) / 2.0 + 0.5,
            /* weight */new Stock(r).getPriceHistory(),
            /* edit */FontAwesome.PENCIL,
            /* delete */FontAwesome.TRASH_O);
        }
 
        sample = grid;
    }
    
}