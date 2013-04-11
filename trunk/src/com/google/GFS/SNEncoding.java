package com.google.GFS;

public class SNEncoding {
	private String sn;
    private String tempStr = "";
    private String tempNum = "";
    private char[] key = { 'S', 'E', 'V', 'N', 'H', 'L', 'I', 'G', 'T', 'O' };
    private int[] keyindex = { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,-1,-1,-1,-1};
    private char[] numkey = { '5', '6', '7', '8', '9', '0', '1', '3', '2', '4' };
    private char[] filterStr = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I',
            'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
            'W', 'X', 'Y', 'Z' };
    private char[] filterStrtoNum = { '9', '0', '7', '2', '4', '9', '6', '3',
            '2', '8', '6', '3', '6', '7', '3', '3', '5', '4', '6', '5', '7',
            '4', '3', '2', '1', '1' };
            
           
            
    /**
     * dispose
     * @return 
     */
    public String getSn(String raw_mac_addr) 
    {
        try {
            sn = getFilterSnOut(raw_mac_addr);
            if (sn.equals("error")) {
                return sn;
            }
            sn = filter1(sn);
            if (sn.equals("error")) {
                return sn;
            }
            sn = filter2(sn);
            if (sn.equals("error")) {
                return sn;
            }
            sn = filter3(sn);
            sn=String.valueOf(filterhash(sn));
            sn=filterconvert(sn);
            return sn;
        } catch (Exception ex) {
            return "error";
        }

    }

    /**
     * filter unneeded character
     * @param 
     * @return
     */
    private String getFilterSnOut(String str) 
    {   	
    
        return str.replace(":", "").replace(" ", "").toUpperCase();
       
    }

    /**
     * filter charater, make number
     * @param parm
     * @return
     */
    private String filter1(String parm) 
    {
        int tempi = -1;
        tempStr = "";
        tempNum = "";
        for (int i = 0; i < parm.length(); i++) {
            tempi = -1;
            for (int i2 = 0; i2 < filterStr.length; i2++) {
                if (filterStr[i2] == parm.charAt(i)) {
                    tempi = i2;
                }
            }
            if (tempi >= 0 && tempi <= 26) {
                tempStr = tempStr + filterStrtoNum[tempi];
            }
            if (tempi == -1) {
                tempNum = tempNum + parm.charAt(i);
            }

        }
        tempStr = tempStr + tempNum;
        return tempStr;

    }

    /**
     * reverse String 
     * @param parm
     * @return
     */
    private String filter2(String parm) 
    {
        tempStr = "";
    
        for (int i = parm.length() - 1; i >= 0; i--) {
            tempStr = tempStr + parm.charAt(i);
        }
        return tempStr;
    }
    /**
     * generate key
     * @param parm
     * @return
     */
    private String filter3(String parm) 
    {
        tempStr = "";
        // fill keyindex;
        for (int i = 0; parm.length() > i; i++) {
            for (int i2 = 0; numkey.length > i2; i2++) {
                try {
                    Integer.valueOf(parm.charAt(i)).intValue();
                } catch (Exception ex) {
                    return "error";
                }
                if (numkey[i2] == Integer.valueOf(parm.charAt(i)).intValue()) {
                    keyindex[i] = i2;
                }
            }
        }
        // get key
        for (int i = 0; i < keyindex.length; i++) {
            if (keyindex[i] != -1) {
                tempStr = tempStr + key[keyindex[i]];
            }
        }
        return tempStr;
    }
    /**
     * encoding
     * @param s
     * @return
     */
    private  int filterhash(String s)     
    { //filter4
        int i=0;
        char ac[]=s.toCharArray();
        int j=0;
        for (int k=ac.length;j<k; j++)
          i=31*i +ac[j];
        return Math.abs(i);
    }  
    /**
     * End filter
     * @param s
     * @return
     */
    private  String filterconvert(String s)     
    { //end filter
        if(s==null || s.length()==0)
            return s;
        byte abyte0[]=s.getBytes();
        char ac[]=new char[s.length()];
        int i =0;
        for(int k=abyte0.length;i<k;i++)
        {
            int j=abyte0[i];
            if(j >=48 && j<=57)
              j=((j-48)+5)%10 + 48;
            else if(j >=65 && j<=90)
              j=((j-65)+13)%26 +65;
            else if(j >=97 && j<=122)
              j=((j-97)+13)%26+97;
          ac[i]=(char)j;
      }  
        return String.valueOf(ac);
    } 

}
