import java.util.Arrays;

public class TrainingImage {
	String person;
	boolean sunglasses;
	Class mood;
	byte[] byteImage;
	double[] normalizedImage;
	int idLocal;
	int id;

	public TrainingImage(String person, boolean sunglasses, Class mood, byte[] image, int idLocal, int id){
		this.person = person;
		this.sunglasses = sunglasses;
		this.mood = mood;
		this.byteImage = image;
		this.idLocal = idLocal;
		this.id = id;
	}

	public double[] getExpectedOutput() {
		switch (mood) {
		case NEUTRAL: 
			return new double[]{1,0,0,0};
		case HAPPY: 
			return new double[]{0,1,0,0};
		case SAD:
			return new double[]{0,0,1,0};
		case ANGRY: 
			return new double[]{0,0,0,1};
		default: 
			return null;
		}
	}

	@Override
	public String toString() {
		return "TrainingImage [person=" + person + ", sunglasses=" + sunglasses + ", mood=" + mood + ", image="
				+ byteImage + ", idLocal=" + idLocal + ", id=" + id + "]";
	}


	public String getPerson() {
		return person;
	}

	public void setPerson(String person) {
		this.person = person;
	}

	public boolean isSunglasses() {
		return sunglasses;
	}

	public void setSunglasses(boolean sunglasses) {
		this.sunglasses = sunglasses;
	}

	public Class getMood() {
		return mood;
	}

	public void setMood(Class mood) {
		this.mood = mood;
	}


	public byte[] getByteImage() {
		return byteImage;
	}


	public void setByteImage(byte[] byteImage) {
		this.byteImage = byteImage;
	}


	public double[] getNormalizedImage() {
		return normalizedImage;
	}


	public void setNormalizedImage(double[] normalizedImage) {
		this.normalizedImage = normalizedImage;
	}


}
