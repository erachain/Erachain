package utils;

public class M_Integer {
   
    // return roud to alvays UP
    public static Integer roundUp(float f){
        int p;
        try {
            p = (int)Math.round((float)f);
            if (  (float) p < f) p++;
          
        } catch (Exception e) {
            // TODO Auto-generated catch block
           // e.printStackTrace();
            p=0;
        }
        return p;
    }
}
