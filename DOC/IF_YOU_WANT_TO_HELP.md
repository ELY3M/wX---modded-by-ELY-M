If you are interested in contributing to "wX" in some way - thank-you. I appreciate your interest.

From 2013-2018 this program was developed by one person (save the code mentioned in the "THANKS"
file) and as such despite valued feedback from users it was developed without a framework to allow
code submissions for others. Given recent interest I will work to provide some guidelines for those
that are interested in coding their own features and having them added to "wX". Please recognize
that I am mostly offering these guidelines to protect the legal and maintainability interest for the
source code itself. Additionally as the till now *only* developer on the project my time is very
limited so I am offering these guidelines to help both the main maintainer (me) and any code
contributors (you) succeed.

With that said, please respect these items:

* Legal aspects - please respect the copyright and trademark interests for all source code files.
  For newly contributed code that are entirely new files it is expected that the licensing terms of
  the project will be followed and that is the GNU GPL v3. Each new file must at the top show the
  license and the copyright owner. There must be no doubt as to where the code came from and who
  wrote it.

* Timelines - I am not able to provide any ETA for code contributions being integrated into my
  branch since I do this as a hobby in my spare time.

* Priorities - My priorities are fixing bugs and keeping updated with Google's various Android and
  tooling updates. New features or enhancements come after that.

* Users first - Any change that impacts the default user experience will be looked at closely.
  With ~10,000 active users their experience needs to come first. If a new feature is added that
  might change that it for example would be done via an option that is turned off by default.

* Comments - I will admit that my code does not have many comments. I never expected it would get to
  where it has gotten. With that said I am working to comment better and to retrofit comments. I
  expect you to do the same.

* Coding style - I can only integrate code that follows similar code style, indentation, and
  follows "Camel case" for variable/method naming. In general please make sure the contributions
  look like the existing code base.

* Lint checks - Please make sure any reasonable issues that Lint checks are pointing out are
  resolved before submission.

* Bugs - If you submit a new feature and there are manual or automated user bug reports please
  understand that I will request but not expect your assistance to fix bugs in your code. One reason
  it might take to have code integrated is because I need to be comfortable with it in case any
  contributor decides to leave the project so to speak and not be available to fix bugs in their
  code.

* It is a goal to keep as much data within the private storage space of the application if possible.
  Any writing to external storage space ( accessible via other apps, etc ) must be done with very
  good reason, ie no alternative.

* In general, the apps basic function should occur without any needed permissions from the user. For
  example to check the weather/radar it should not be necessary to have access to users GPS location
  or to write to their external storage. Such permissions should be requested later on as add needed
  basis for advanced functionality.

* All contributors will need to electronically sign for a Contributor License Agreement to protect
  both parties and the code:
  https://gitlab.com/joshua.tee/wx/blob/master/DOC/CLA.md

Send an email to joshua.tee@gmail.com from the email account you will correspond with her me. In the
email indicate your GitHub or Gitlab username as another identifier. If you agree state the
following in the email:

-------
I accept the following contributor license agreement agreement.
------ ( paste the entire CLA as found at the URL above and paste in below this statement

* Please respect the privacy policy. No code will be accepted that does not respect the privacy
  policy:
  https://gitlab.com/joshua.tee/wx/blob/master/DOC/PRIVACY_POLICY_FOR_WX.txt

* Please respect the code of conduct as documented here:
  https://gitlab.com/joshua.tee/wx/blob/master/DOC/CODEOFCONDUCT.md

* For the time being I will merge all patches myself into the main branch.
