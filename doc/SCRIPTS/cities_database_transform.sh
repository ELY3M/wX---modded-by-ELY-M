#!/bin/bash
#
# brew install gnu-sed
# brew install coreutils
#
#
#
#
#echo Manual correction needed: San Francisco, 37.787239,-122.4581,805235
export LC_ALL='C'
dataFileCompressed=Gaz_places_national.txt.gz
dataFile=Gaz_places_national.txt
targetFile=cityall.txt

gzip -d ${dataFileCompressed}
dos2unix ${dataFile}
awk -F'\t' '{print $1", "$4","$13","$14","$7}' ${dataFile} | gsed 's/ *,/,/g' | gsed 's/ city,/,/' \
        | gsed 's/ CDP,/,/' | gsed 's/ borough,/,/' | gsed 's/ town,/,/' | gsed 's/ village,/,/' \
       | gsed 's/ comunidad,/,/' | gsed 's/ urbana,/,/'  | gsed 's/ zona,/,/'  \
	| gsed 's/ city (balance),/,/' | gsed 's/ metropolitan government (balance),/,/' | gsed 's/County consolidated government (balance),/,/' \
	| gsed 's/ metro government (balance),/,/' | gsed 's/ municipality,/,/' \
	| gsed 's/-Clarke County unified government (balance),/,/' \
	| gsed 's/Urban //' \
	| gsed 's/-Fayette urban county,/,/' \
	| gsed 's/-Richmond ,/,/' \
	| gsed 's/ municipality,/,/' \
	| gsed 's/ city and,/,/' \
	| gsed 's/-Silver Bow (balance),/,/' \
	| gsed 's/Louisville\/Jefferson County,/Louisville,/' \
	| gsed 's/Nashville-Davidson,/Nashville,/' \
	| gsed 's/37.727239/37.787239/' \
	| gsed 's/-123.032229/-122.4581/' \
        | gsort -r -n -t, -k5 > ${targetFile}

gzip ${dataFile}
echo Output file: ${targetFile}
