package lab3;

class Result {
    private double[][] data;

    public Result(int rows, int cols) {
        this.data = new double[rows][cols];
    }

    public double[][] getData() {
        return this.data;
    }

    public void setData(double[][] data) {
        this.data = data;
    }

    public void display() {
        for (double[] row : this.data) {
            for (double value : row) {
                System.out.printf("%10.4f", value);
            }
            System.out.println();
        }
    }
}
