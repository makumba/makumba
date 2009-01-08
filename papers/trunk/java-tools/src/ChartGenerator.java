import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;

/**
 * @author Rudolf Mayer
 * @version $Id: $
 */
public class ChartGenerator {

	private static final class CategoryNumberTickUnit extends NumberTickUnit {
		private static final long serialVersionUID = 1L;

		private Map<Double, String> categoryNamesMap;

		public CategoryNumberTickUnit(double size,
				Map<Double, String> categoryNamesMap) {
			super(size);
			this.categoryNamesMap = categoryNamesMap;
		}

		public CategoryNumberTickUnit(double size, double[] values,
				String[] names) {
			super(size);
			this.categoryNamesMap = new HashMap<Double, String>();
			for (int i = 0; i < names.length; i++) {
				categoryNamesMap.put(values[i], names[i]);
			}
		}

		@Override
		public String valueToString(double value) {
			if (categoryNamesMap.get(value) != null) {
				return categoryNamesMap.get(value);
			}
			return super.valueToString(value);
		}
	}

	private static final long serialVersionUID = 1L;

	public static final NumberTickUnit skillsTickUnit = new CategoryNumberTickUnit(
			1, new double[] { 0, 1, 2, 3, 4 },
			new String[] { "No skills", "Little knowl.", "Some experience",
					"Experienced", "Master" });

	public static void main(String[] args) throws FileNotFoundException,
			IOException, InterruptedException {
		boolean showChart = false;

		String path = args[0];

		HSSFWorkbook book = new HSSFWorkbook(new FileInputStream(path
				+ "Makumba Users Survey.xls"));
		final HSSFSheet dataSheet = book.getSheet("DataAverages");

		DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		String[] names = new String[3];
		String[] xCats = { "Start", "Year 1", "Year 2", "Year 3", "Year 4" };
		short[] starts = { 1, 7, 13 };

		String title = "learning-technologies";
		final HSSFRow namesRow = dataSheet.getRow(1);
		final HSSFRow dataRow = dataSheet.getRow(33);
		for (int j = 0; j < starts.length; j++) {
			names[j] = namesRow.getCell(starts[j]).getRichStringCellValue()
					.getString();
			for (short i = 0; i < xCats.length; i++) {
				dataset.addValue(dataRow.getCell((short) (i + starts[j]))
						.getNumericCellValue(), names[j], xCats[i]);
			}
		}
		final String chartsPath = path + "/charts/";
		new File(chartsPath).mkdirs();
		createChart(chartsPath, title, showChart, dataset, skillsTickUnit);

		DefaultCategoryDataset datasetAll = new DefaultCategoryDataset();
		for (short s : new short[] { 3, 5, 6, 16, }) {
			final HSSFRow row = dataSheet.getRow(s + 3);
			String person = row.getCell((short) 0).getRichStringCellValue()
					.getString();
			DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
			for (int i = 0; i < starts.length; i++) {
				final double padding = i * -0.02;
				for (short k = 0; k < xCats.length; k++) {
					final HSSFCell cell = row.getCell((short) (starts[i] + k));
					if (cell != null) {
						dataset2.addValue(cell.getNumericCellValue() + padding,
								names[i], xCats[k]);
						datasetAll.addValue(cell.getNumericCellValue()
								+ padding, names[i], xCats[k]);
					} else {
						dataset2.addValue(null, names[i], xCats[k]);
					}
				}
			}
			if (dataset2.getColumnCount() > 1) {
				createChart(chartsPath, person.split(" ")[0], showChart,
						dataset2, skillsTickUnit);
			} else {
				System.out.println("Skipping " + person + ", columns: "
						+ dataset2.getColumnCount());
			}
		}

		// createChart(path + "/charts/", "All", showChart, datasetAll);
		if (!showChart) {
			System.exit(0); // some process seems to be hanging..
		}

	}

	private static void createChart(String path, String title,
			boolean showChart, final DefaultCategoryDataset dataset,
			NumberTickUnit tickUnit) throws IOException {
		JFreeChart chart = ChartFactory.createLineChart("", "", "", dataset,
				PlotOrientation.VERTICAL, true, true, false);

		// set the background color for the chart...
		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...

		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.gray);
		plot.setRangeGridlinePaint(Color.gray);

		BasicStroke stroke2 = new BasicStroke(2, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_BEVEL, 0, new float[] { 3, 8, 3, 8 }, 0);
		BasicStroke stroke3 = new BasicStroke(2, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_BEVEL, 0, new float[] { 2, 2, 3, 3 }, 0);
		BasicStroke[] strokes = { new BasicStroke(2), stroke2, stroke3 };

		final LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot
				.getRenderer();
		for (int i = 0; i < dataset.getRowCount(); i++) {
			renderer.setSeriesPaint(i, Color.black);
			renderer.setSeriesStroke(i, strokes[i % strokes.length]);
		}

		final CategoryAxis domainAxis = plot.getDomainAxis();
		// domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI
		// / 3.0));

		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setRange(new Range(0, 4.25));

		rangeAxis.setTickUnit(tickUnit);

		// increase the default font for the category lavels
		domainAxis
				.setTickLabelFont(biggerFont(domainAxis.getTickLabelFont(), 2));
		rangeAxis.setTickLabelFont(biggerFont(domainAxis.getTickLabelFont(),
				1.0));
		final LegendTitle legend = chart.getLegend();
		legend.setItemFont(biggerFont(legend.getItemFont(), 1.7));
		legend.setWidth(legend.getWidth() * 1.5);
		// legend.setFrame(BlockBorder.NONE);
		legend.setItemLabelPadding(new RectangleInsets(0, 30, 0, 30));
		legend.setWidth(800);

		final ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(800, 500));

		JFrame frame2 = new JFrame(title);
		frame2.setContentPane(chartPanel);
		frame2.pack();
		frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		String pngFileName = path + title;

		System.out.println("Saving image to file: " + pngFileName);
		ChartUtilities.saveChartAsPNG(new File(pngFileName + ".png"), chart,
				chartPanel.getWidth(), chartPanel.getHeight());

		if (false) {
			// hide range axis labels, save again
			rangeAxis.setTickLabelsVisible(false);
			ChartUtilities.saveChartAsPNG(new File(pngFileName
					+ "_noRangeAxis.png"), chart, chartPanel.getWidth(),
					chartPanel.getHeight());
			// hide domain axis labels, save again
			domainAxis.setTickLabelsVisible(false);
			ChartUtilities.saveChartAsPNG(
					new File(pngFileName + "_noAxis.png"), chart, chartPanel
							.getWidth(), chartPanel.getHeight());
		}
		if (showChart) {
			frame2.setVisible(true);
		}
	}

	private static Font biggerFont(Font font, double factor) {
		return new Font(font.getName(), font.getStyle(),
				(int) (font.getSize() * factor));
	}

}
