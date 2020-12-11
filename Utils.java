import java.awt.*;
import java.util.*;
import java.util.List;

public class Utils {
	private static final Random rand = new Random();

	public static List<Point> generateRandomPoints(final int numPoints) {
		List<Point> points = new ArrayList<>();
		for(int i = 0; i < numPoints; ++i) {
			double x = rand.nextDouble();
			double y = rand.nextDouble();
			final Point point = new Point(x, y);
			points.add(point);
		}

		return points;
	}

	public static Map<Point, Set<Point>> generateRandomClusters(final int k, List<Point> points) {
	    Map<Point, Set<Point>> clusters = new HashMap<>();

		for(int i = 0; i < k; ++i) {
			double x = rand.nextDouble();
			double y = rand.nextDouble();
			final Point centroid = new Point(x, y);
			clusters.put(centroid, new HashSet<>());
		}

		final int chunkSize = (k + points.size()) / k;
		int i = 0;
		for(Map.Entry<Point, Set<Point>> entry: clusters.entrySet()) {
			final int start = i * chunkSize;
			int end = (i + 1) * chunkSize - 1;
			if(end >= points.size()) end = points.size();

			List<Point> partitionedPoints = points.subList(start, end);
			for(Point point: partitionedPoints)
				entry.getValue().add(point);
			i++;
		}

		return clusters;
	}

	private static List<Point> getPartitionedPoints(List<Point> points, int start, int end) {
	    return points.subList(start, end);
	}

	public static double getEuclideanDist(Point a, Point b) {
		double val1 = Math.pow((a.x - b.x), 2);
		double val2 = Math.pow((a.y - b.y), 2);
		return Math.sqrt(val1 + val2);
	}

	// ONLY SUPPORT UP TO 16 CLUSTERS.
	public static void draw(Map<Point, Set<Point>> clusters) {
		Map<Integer, Color> colors = initColorMap();

		StdDraw.setScale(-.05, 1.05);
		StdDraw.line(0,0,1.0,0);
		StdDraw.line(0,0,0,1.0);

		int count = 0;
		for(Map.Entry<Point, Set<Point>> cluster: clusters.entrySet()) {
			if(count >= 17) return;

			StdDraw.setPenColor(colors.get(count++));

			Point centroid = cluster.getKey();
			StdDraw.circle(centroid.x, centroid.y,0.005);
			StdDraw.filledCircle(centroid.x, centroid.y, 0.005);

			for(Point point: cluster.getValue())
				StdDraw.point(point.x, point.y);
		}
	}

	private static Map<Integer, Color> initColorMap() {
		Map<Integer, Color> colors = new HashMap<>();
		colors.put(0, StdDraw.BLACK);
		colors.put(1, StdDraw.BLUE);
		colors.put(2, StdDraw.CYAN);
		colors.put(3, StdDraw.DARK_GRAY);
		colors.put(4, StdDraw.GRAY);
		colors.put(5, StdDraw.GREEN);
		colors.put(6, StdDraw.LIGHT_GRAY);
		colors.put(7, StdDraw.MAGENTA);
		colors.put(8, StdDraw.ORANGE);
		colors.put(9, StdDraw.PINK);
		colors.put(10, StdDraw.RED);
		colors.put(11, StdDraw.WHITE);
		colors.put(12, StdDraw.YELLOW);
		colors.put(13, StdDraw.BOOK_BLUE);
		colors.put(14, StdDraw.BOOK_LIGHT_BLUE);
		colors.put(15, StdDraw.BOOK_RED);
		colors.put(16, StdDraw.PRINCETON_ORANGE);

		return colors;
	}
}