#!/usr/bin/perl


# script to break down census state lines into 52 files one per area for easier processing
# takes stdin  ie. ./script < gz_2010_us_040_00_20m.kml
# creates 52 files in curr direction that must be put into res/raw

while (<>)
{
   if ( $_ =~ /coordinates/ )
   {
    $i++;
        my $filename = "state$i.txt";
        my $filename1 = "state$i";
        my $filename2 = "s$k";
        open(my $fh, '>', $filename) or die "Could not open file '$filename' $!";
        print $fh $_;
        close $fh;
        print "cod_hash.add(R.raw.$filename1);\n";
        print "~/android-sdk-linux/platform-tools/adb pull /data/data/joshuatee.wx/files/$filename2\n";
        #print "cod_hash.add(R.raw.$filename1);\n"

     $k++;

   }
}


print $i + "\n";



#         cod_hash.put("ma",R.raw.ma);
