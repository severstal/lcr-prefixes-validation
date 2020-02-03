
const HOST = 'http://10.28.101.14:7001'; // todo get from env.prop at build   test: 10.28.101.14:7001
const HOME_PATH = '/lcr-prefixes-validation';

export const LCR_PREPARE_URL = HOST + HOME_PATH + '/api/lcr-data-prepare';
export const GET_LCR_FILTERS_URL = HOST + HOME_PATH + '/api/get-lcr-filters';
export const GET_LCR_DATA_URL = HOST + HOME_PATH + '/api/get-lcr-data';
export const VERIFY_BY_PREFIXES_URL = HOST + HOME_PATH +  '/api/verify-lcr-by-prefixes';
export const VERIFY_BY_COUNT_URL = HOST + HOME_PATH +  '/api/verify-lcr-by-count';
export const GET_REPORT_BY_PREFIXES_URL = HOST + HOME_PATH +  '/api/get-report-by-prefixes';
export const GET_REPORT_BY_COUNT_URL = HOST + HOME_PATH +  '/api/get-report-by-count';
export const MITM_URL = 'mitm/mitm.html';

export const VERIFICATION_TYPE = {
    byPrefixes: 'byPrefixes',
    byCount: 'byCount'
};

