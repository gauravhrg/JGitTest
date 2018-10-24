*** Settings ***
Resource          ../../../../../resources/global_resource.txt

*** Keywords ***
Save Fx For Historical Dates
    [Arguments]    ${v Fx Curve}    ${v Fx Value}    ${v COB Date}
    Forward Fx Rates    ${v Fx Curve}    Spot=${v Fx Value}
    ${Success Failure}    Run EOD Processes    COB Date=${v COB Date}    Market Data Snapshot=CXL_38997_Snapshot    Process Group=CXL_38997_PrcGrp    Mark Curves COB=1    Forcefull MCC=1

Suite Tear Down
    Forward Fx Rates    In GBP Per USD    Spot=1.05
