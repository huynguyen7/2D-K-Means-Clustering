public class Point implements Comparable {
	public double x;
	public double y;

	private Point() {}
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return String.format("[%f, %f]", x, y);
	}

	@Override
	public int compareTo(Object o) {
	    Point another = (Point) o;
	    if(this.x == another.x) {
	    	return Double.compare(this.y, another.y);
		} else return Double.compare(this.x, another.x);
	}
}
