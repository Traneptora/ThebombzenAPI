package thebombzen.mods.thebombzenapi.configuration;

public class CompoundExpression<U> implements BooleanTester<U> {
	public static final int PURE = 0;
	public static final int AND = 1;
	public static final int OR = 2;
	public static final int NOT = 3;
	public static final int XOR = 4;
	
	protected int type;
	protected BooleanTester<? super U> first = null;
	protected BooleanTester<? super U> second = null;
	
	public CompoundExpression(BooleanTester<? super U> value){
		type = PURE;
		this.first = value;
	}
	
	public CompoundExpression(int type, BooleanTester<? super U> first, BooleanTester<? super U> second){
		switch (type){
		case AND:
		case OR:
		case NOT:
		case XOR:
			break;
		default:
			throw new IllegalArgumentException();
		}
		this.type = type;
		this.first = first;
		this.second = second;
	}
	
	@Override
	public boolean contains(U u){
		switch (type){
		case PURE:
			return first.contains(u);
		case AND:
			return first.contains(u) && second.contains(u);
		case OR:
			return first.contains(u) || second.contains(u);
		case NOT:
			return !first.contains(u);
		case XOR:
			return first.contains(u) ^ second.contains(u);
		}
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CompoundExpression<?> other = (CompoundExpression<?>) obj;
		if (first == null) {
			if (other.first != null)
				return false;
		} else if (!first.equals(other.first))
			return false;
		if (second == null) {
			if (other.second != null)
				return false;
		} else if (!second.equals(other.second))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		result = prime * result + type;
		return result;
	}
	
	@Override
	public String toString(){
		switch (type){
		case AND:
			return "(" + first.toString() + ")&(" + second.toString() + ")";
		case OR:
			return "(" + first.toString() + ")|(" + second.toString() + ")";
		case XOR:
			return "(" + first.toString() + ")^(" + second.toString() + ")";
		case NOT:
			return "!(" + first.toString() + ")";
		case PURE:
			return first.toString();
		default:
			return null;
		}
	}
	
}
