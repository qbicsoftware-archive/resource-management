/*******************************************************************************
 * QBiC Calendar provides an infrastructure for defining calendars for specific purposes like
 * booking devices or planning resources for services and integration of relevant data into the
 * common portal infrastructure. Copyright (C) 2017 Aydın Can Polatkan & David Wojnar
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see http://www.gnu.org/licenses/.
 *******************************************************************************/
package facs.test;

import java.util.Random;

import com.vaadin.server.FontAwesome;
import com.vaadin.server.FontIcon;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Notification;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickEvent;
import com.vaadin.ui.renderers.ClickableRenderer.RendererClickListener;
import com.vaadin.ui.renderers.HtmlRenderer;
import com.vaadin.ui.renderers.NumberRenderer;
import com.vaadin.ui.renderers.ProgressBarRenderer;


public class GridTest {


  public static class BoldLastNameRenderer extends HtmlRenderer {

    @Override
    public elemental.json.JsonValue encode(String value) {
      int lastSpace = value.lastIndexOf(' ');
      if (lastSpace >= 0) {
        value =
            String.format("<span style=\"pointer-events: none;\">%s<b>%s</b></span>",
                value.substring(0, lastSpace), value.substring(lastSpace));
      }
      return super.encode(value);
    }
  }

  public GridRendererSample() {

    Grid grid = new Grid();
    grid.setSizeFull();

    grid.addColumn("index", Integer.class).setRenderer(new NumberRenderer("%02d"))
        .setHeaderCaption("##").setExpandRatio(0);

    grid.getColumn("index").setWidth(50);

    grid.addColumn("name", String.class).setRenderer(new BoldLastNameRenderer()).setExpandRatio(2);

    grid.addColumn("progress", Double.class).setRenderer(new ProgressBarRenderer())
        .setExpandRatio(2);

    grid.addColumn("weight", double[].class).setRenderer(new SparklineRenderer()).setExpandRatio(4);

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
