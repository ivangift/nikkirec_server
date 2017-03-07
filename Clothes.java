public class Clothes
{
	private static final int BLOCK = 5;
  int width;
  int height;
  byte[] data;
  int[] features; // rgb

  /**
   * w and h should be multiple of 5, don't ask me why
   */
  public Clothes(int w, int h, byte[] data) {
  	this.width = w;
  	this.height = h;
  	this.data = data;
  	initFeature();
  }

  private void initFeature() {
  	int w = width / BLOCK;
  	int h = height / BLOCK;
  	int len = (width / BLOCK) * (height / BLOCK);
  	features = new int[len * 3];
  	for (int i = 0; i < height; i ++) {
  		for (int j = 0; j < width; j ++) {
  			if (i < w * BLOCK && j < h * BLOCK) {
  				int idx = (width * i + j) * 3;
  				int fidx = (w * (i / BLOCK) + j / BLOCK) * 3;
  				for (int k = 0; k < 3; k++) {
  					features[fidx + k] += data[idx + k] & 0xFF;
	  			}
				}
  		}
  	}
  }

  /**
   * Fast but rough
   */
  /*
  public double similarity(Clothes other) {
  	double sim = 0;
  	if (features.length != other.features.length) {
  		return 1e20; // should be very large
  	}
  	for (int i = 0; i < features.length; i++) {
  		int diff = features[i] - other.features[i];
  		sim += diff * diff;
  	}
  	return sim;
  }*/

  public int similarity(Clothes other) {
  	int count = 0;
  	if (features.length != other.features.length) {
  		return 1<<30;
  	}
  	for (int i = 0; i < data.length; i += 3) {
  		double sim = 0;
  		for (int j = i; j < i + 3; j++) {
  			int a = data[i] & 0xFF;
	  		int b = other.data[i] & 0xFF;
	  		int diff = b-a;
	  		sim += diff * diff;
  		}
  		if (sim < 50) {
				count ++;
			}
  	}
  	return -count;
  }

  /**
   * Slow but accurate (relatively...)
   */
  public int similarityDetails(Clothes other) {
  	int count = 0;
  	if (data.length != other.data.length) {
  		return 1<<30;
  	}
  	for (int i = 0; i < data.length; i += 3) {
  		double sim = 0;
  		for (int j = i; j < i + 3; j++) {
  			int a = data[i] & 0xFF;
	  		int b = other.data[i] & 0xFF;
	  		int diff = b-a;
	  		sim += diff * diff;
  		}
  		if (sim < 20) {
				count ++;
			}
  	}
  	return -count;
  }

  public static void main(String[] args) throws java.lang.Exception {
  	// unit tests
  	byte[] data = new byte[12 * 12 * 3];
  	for (int i = 0; i < 12; i++) {
  		for (int j = 0; j < 12; j++) {
  			int k = (i * 12 + j) * 3;
  			data[k] = (byte) (i * j * 2);
  			data[k + 1] = (byte) (i * j * 2 + 1);
  			data[k + 2] = (byte) (i * j * 2 + 2);
  			System.err.print(data[k] + " " + data[k + 1] + " " + data[k + 2] + " ");
  		}
  		System.err.println();
  	}
  	Clothes c = new Clothes(12, 12, data);
  	assert c.features.length == 2 * 2 * 3; // 2x2 with 3 colors
  	for (int i = 0; i < 2; i++) {
  		for (int j = 0; j < 2; j++) {
  			int idx = (i * 2 + j) * 3;
  			System.err.print(c.features[idx] + " " + c.features[idx + 1] + " " + c.features[idx + 2] + " ");
  		}
  		System.err.println();
  	}
  }

}