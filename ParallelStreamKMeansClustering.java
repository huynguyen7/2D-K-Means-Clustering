import java.util.*;
import java.util.concurrent.*;

public class ParallelStreamKMeansClustering {
    public static void main(String[] args) {
        Scanner scnr = new Scanner(System.in);
        System.out.println("\t\t----2D-K-MEANS-CLUSTERING----");

        // Get user's inputs.
        System.out.print("INPUT K = ");
        final int k = scnr.nextInt();

        if(k <= 0) {
            System.out.println("INVALID INPUT K.");
            return;
        }

        System.out.print("NUM POINTS = ");
        final int numPoints = scnr.nextInt();

        if(numPoints <= 0) {
            System.out.println("INVALID INPUT NUM POINTS.");
            return;
        }

        System.out.print("MAX ITERATIONS = ");
        final int MAX_ITERATIONS = scnr.nextInt();
        if(MAX_ITERATIONS <= 0) {
            System.out.println("INVALID INPUT MAX ITERATIONS.");
            return;
        }

        scnr.close();

        // Generate random clusters.
        List<Point> points = Utils.generateRandomPoints(numPoints);
        Map<Point, Set<Point>> clusters = Utils.generateRandomClusters(k, points);

        ParallelStreamKMeansClustering app = new ParallelStreamKMeansClustering(k, points, MAX_ITERATIONS, clusters);
		try {
        	Map<Point, Set<Point>> results = app.start();
        	Utils.draw(results);
		} catch(Exception e) {}
    }

    private final int k;
    private List<Point> points;
    private Map<Point, Set<Point>> clusters;
    private final ForkJoinPool service;
    private final int MAX_ITERATIONS;

    public ParallelStreamKMeansClustering(final int k, List<Point> points, final int MAX_ITERATIONS,
                                          Map<Point, Set<Point>> clusters) {
        this.k = k;
        this.points = points;
        this.MAX_ITERATIONS = MAX_ITERATIONS;
        this.clusters = clusters;
        service = ForkJoinPool.commonPool();
    }

    public Map<Point, Set<Point>> start() throws ExecutionException, InterruptedException {
        double startTime = System.nanoTime();
        parallelStreamKMeansClustering();
        double endTime = System.nanoTime();
        double timeTaken = (endTime - startTime) / 1e6; // milliseconds
        System.out.printf("-> PARALLEL STREAM K-MEANS CLUSTERING:" +
                "\n\tTIME_TAKEN = %10.3f ms.\n", timeTaken);

        return clusters;
    }

    public void parallelStreamKMeansClustering() throws ExecutionException, InterruptedException {
        Set<Point> newCentroids = new HashSet<>();

        int counter = 0;
        do {
            newCentroids.clear();
            // GET NEW CENTROIDS
            for (Map.Entry<Point, Set<Point>> cluster: clusters.entrySet()) {
                Set<Point> clusterPoints = cluster.getValue();
                Future<Double> xTask = service.submit(new XTask(clusterPoints));
                Future<Double> yTask = service.submit(new YTask(clusterPoints));

                newCentroids.add(new Point(xTask.get(), yTask.get()));
            }
        } while(counter++ < MAX_ITERATIONS && hasNewCentroids(newCentroids));

        service.shutdown();
    }

    private boolean hasNewCentroids(Set<Point> newCentroids) {
        for(Map.Entry<Point, Set<Point>> cluster: clusters.entrySet()) {
            if(!newCentroids.contains(cluster.getKey())) { // have new centroid
                reassignPointsToNewCentroids(newCentroids);
                return true;
            }
        }

        return false;
    }

    private void reassignPointsToNewCentroids(Set<Point> newCentroids) {
        clusters.clear(); // Flush out all previous centroids.

        for(Point centroid: newCentroids) {
            if(centroid == null) continue;
            clusters.put(centroid, new HashSet<>());
        }

        for(Point point: points) {
            Point minDistPoint = null;
            double minDist = Double.MAX_VALUE;

            // Look for closest centroid
            for(Point centroid: newCentroids) {
                // Apply Euclidean Distance.
                double dist = Utils.getEuclideanDist(point, centroid);
                if(dist < minDist) {
                    minDist = dist;
                    minDistPoint = centroid;
                }
            }

            clusters.get(minDistPoint).add(point);
        }
    }

    private class XTask extends RecursiveTask<Double> {
        private Set<Point> clusterPoints;

        public XTask(Set<Point> clusterPoints) {
            this.clusterPoints = clusterPoints;
        }

        @Override
        protected Double compute() {
            return clusterPoints.parallelStream()
                    .mapToDouble(pt -> pt.x)
                    .reduce(0, Double::sum) / (double) clusterPoints.size();
        }
    }

    private class YTask extends RecursiveTask<Double> {
        private Set<Point> clusterPoints;

        public YTask(Set<Point> clusterPoints) {
            this.clusterPoints = clusterPoints;
        }

        @Override
        protected Double compute() {
            return clusterPoints.parallelStream()
                    .mapToDouble(pt -> pt.y)
                    .reduce(0, Double::sum) / (double) clusterPoints.size();
        }
    }
}
