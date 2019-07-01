//
// Created by Matthias on 01.03.2018.
//

#include "PitchDetector.h"
#include "dywapitchtrack.h"
//#include <mutex>


static dywapitchtracker* mdywapitchtracker;
//std::mutex mtx;

PitchDetector::PitchDetector() {
    mdywapitchtracker = new dywapitchtracker;
}

double PitchDetector::computePitch(double * samples, int startsample, int samplecount) {
    double pitch = dywapitch_computepitch(mdywapitchtracker, samples, startsample, samplecount);
    return pitch;
}

double PitchDetector::testMe(double * samples, int samplecount) {
    return dywapitch_tester(samples, samplecount);
}