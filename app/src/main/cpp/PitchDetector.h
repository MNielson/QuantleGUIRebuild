//
// Created by Matthias on 01.03.2018.
//

#ifndef MYAPPLICATION2_PITCHDETECTOR_H
#define MYAPPLICATION2_PITCHDETECTOR_H
class PitchDetector {
public:
    PitchDetector();
    static double computePitch(double * samples, int startsample, int samplecount);
    static double testMe(double * samples, int samplecount);
};
#endif //MYAPPLICATION2_PITCHDETECTOR_H
