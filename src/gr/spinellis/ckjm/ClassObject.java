package gr.spinellis.ckjm;

public class ClassObject {
		private String className;
		private int WMC;
		private int DIT;
		private int NOC;
		private int CBO;
		private int RFC;
		private int LCOM;
		private int Ca;
		private int NPM;
		
		public ClassObject(){
			
		}

		public ClassObject(String name, int wmc, int dit, int noc, int cbo, int rfc, int lcom,
				int ca, int npm){
			this.className = name;
			this.WMC = wmc;
			this.DIT = dit;
			this.NOC = noc;
			this.CBO = cbo;
			this.RFC = rfc;
			this.LCOM = lcom;
			this.Ca = ca;
			this.NPM = npm;
		}
		
		public String toCsvFormatCommaDelimited(){
			return this.className+","+this.WMC+","+this.DIT+","+this.NOC+","+this.CBO
			+","+this.RFC+","+this.LCOM+","+this.Ca+","+this.NPM;
		}
		
		public String toCsvFormatSemicolonDelimited(){
			return this.className+";"+String.valueOf(this.WMC)+";"+String.valueOf(this.DIT)+";"+String.valueOf(this.NOC)+";"+String.valueOf(this.CBO)
			+";"+String.valueOf(this.RFC)+";"+String.valueOf(this.LCOM)+";"+String.valueOf(this.Ca)+";"+String.valueOf(this.NPM);
		}
		
		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public int getWMC() {
			return WMC;
		}

		public void setWMC(int wMC) {
			WMC = wMC;
		}

		public int getDIT() {
			return DIT;
		}

		public void setDIT(int dIT) {
			DIT = dIT;
		}

		public int getNOC() {
			return NOC;
		}

		public void setNOC(int nOC) {
			NOC = nOC;
		}

		public int getCBO() {
			return CBO;
		}

		public void setCBO(int cBO) {
			CBO = cBO;
		}

		public int getRFC() {
			return RFC;
		}

		public void setRFC(int rFC) {
			RFC = rFC;
		}

		public int getLCOM() {
			return LCOM;
		}

		public void setLCOM(int lCOM) {
			LCOM = lCOM;
		}

		public int getCa() {
			return Ca;
		}

		public void setCa(int ca) {
			Ca = ca;
		}

		public int getNPM() {
			return NPM;
		}

		public void setNPM(int nPM) {
			NPM = nPM;
		}
}
