package uk.gov.hmcts.reform.em.orchestrator.stitching;

public class StitchingServiceException extends RuntimeException {

    public StitchingServiceException(String message) {
        super(message);
    }

    public StitchingServiceException(String message, Throwable cause) {
        super(message, cause);
    }


    public static void main(String[] args) {


        try {
            for (int i = 0; i < 10; i++) {

                if (i == 3) {
                    System.out.println("i---3 ");
                    return;
                }else{

                }
                System.out.println("closeee");
            }
        }finally {
            System.out.println("finally ");
        }
    }
}
