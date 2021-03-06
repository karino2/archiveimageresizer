#pragma version(1)
#pragma rs java_package_name(com.livejournal.karino2.archiveimageresizer)
#pragma rs_fp_relaxed


const float gammaValue = 1.6f;

uchar4 __attribute__((kernel)) fourBitGrayWithGamma(uchar4 in)
{
    float gray = (2*in.r+4*in.g+in.b)/7.0f;
    float withGamma =pow(gray/255.0f, gammaValue)*255.0f;
    float grayVal = clamp(withGamma, .0f, 255.0f);
    // for 4bit color, 0-15 =>0 16-31=>17, ...
    int bitLevel = (int)(grayVal/16.0f);
    uchar4 outVal;
    outVal.r = (uchar)rsClamp(17*bitLevel, 0, 255);
    outVal.g = (uchar)rsClamp(17*bitLevel, 0, 255);
    outVal.b = (uchar)rsClamp(17*bitLevel, 0, 255);
    outVal.a = 255;
    return outVal;
}

