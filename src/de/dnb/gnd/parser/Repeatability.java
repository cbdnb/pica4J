package de.dnb.gnd.parser;

public enum Repeatability {
	REPEATABLE {
		@Override
		public String toString() {
			return "R";
		}
	},
	NON_REPEATABLE {
		@Override
		public String toString() {
			return "NR";
		}
	},
	UNKNOWN {
		@Override
		public String toString() {
			return "?";
		}
	};
}
