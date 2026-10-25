package ebf.tim;

import java.util.Collections;
import java.util.List;

/**
 * What is left of the TiM mod class: Traincraft is a single mod now (see train.Traincraft), this keeps the shared
 * enums and switches the rest of TiM refers to.
 * @author Eternal Blue Flame
 */
public class TrainsInMotion {
    public static final String MODID = "traincraft";

    public enum transportTypes {
        STEAM,DIESEL,HYDROGEN_DIESEL,ELECTRIC,NUCLEAR_STEAM,NUCLEAR_ELECTRIC, //trains
        PASSENGER, FREIGHT, HOPPER, TANKER, WORKCAR, SLUG, B_UNIT, //generic rollingstock
        LOGCAR, RAILCAR, FREEZERCAR, LAVATANKER, GRAINHOPPER, COALHOPPER, OILCAR, //specific cargo rollingstock
        FUELTANKER, TENDER, ELECTRIC_TENDER, JUKEBOX, TRACKBUILDER, OTHER; //specialized Rollingstock

         public boolean isTrain(){
             return this == STEAM || this == DIESEL || this == HYDROGEN_DIESEL || this == ELECTRIC ||
                     this == NUCLEAR_STEAM || this == NUCLEAR_ELECTRIC || this == B_UNIT;
         }
         public boolean isHopper(){
             return this == HOPPER || this == GRAINHOPPER || this == COALHOPPER;
         }
         public boolean isTanker(){
             return this == TANKER || this == LAVATANKER || this == OILCAR || this == FUELTANKER;
         }

         public static boolean isTrain(List<transportTypes> types){
             for(transportTypes t : types){
                 if(t.isTrain()){
                     return true;
                 }
             }
             return false;
         }
         public List<transportTypes> singleton(){return Collections.singletonList(this);}
    }
}
