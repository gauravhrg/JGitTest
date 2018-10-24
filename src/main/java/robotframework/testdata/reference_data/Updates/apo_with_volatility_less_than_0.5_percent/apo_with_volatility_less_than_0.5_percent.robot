*** Settings ***
Resource          ../../../../../resources/global_resource.txt

*** Keywords ***
Test Data
    Create Forward Price Curve    ICE WTI SWP S    Commodity=Oil    UOM=BBL    Currency=USD    Location=ARA
    Create Volatility Curves    VOL NYM WTI FTR    VOL NYM WTI FTR    Base Price Curve= ICE WTI SWP S    Months=36    Strike UOM=BBL    Strike Currency=USD
    ...    Maintain=Relative Strikes - Percentage Differential    Maintain Type=User Input - Absolute Value    Heat Rate Matrix=No    Display Precision=4    Strike Display Precision=4    Exercise Frequency=Monthly
    ...    Put/Call=N/A    Strikes From=-10    Strikes To=10    Strike Interval=2    Delivery Schedule=Monthly
    Update Forward Price Curve With Specific Field    ICE WTI SWP S    Volatility Curve=VOL NYM WTI FTR
    Create Pricing Quote    ICE WTI SWP Quote    Fwd Price Curve=ICE WTI SWP S    Commodity=Oil    UOM=BBL    Currency=USD    Location=ARA
    Save Volatility Price With Time Period Type    VOL NYM WTI FTR    4    Date=20080401000000    StrikeFrom=-10|0.15    ATM=0|0.25    StrikeTo=10|.35
    ${Forward Price Curve Num}    Get Pricing Quote    ICE WTI SWP S
    Save Forward Price    4    ICE WTI SWP S    ${Forward Price Curve Num}    Date=20080401000000    Price =100
