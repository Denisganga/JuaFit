"""Import KUCCPS table cells without inferring grades or current availability."""
import json,re,hashlib,sys
from pathlib import Path
import pdfplumber
source=Path(sys.argv[1]); root=Path(__file__).resolve().parents[1]
clean=lambda s: re.sub(r'\s+',' ',s or '').strip()
records=[]; counts=[]
with pdfplumber.open(source) as pdf:
 for page_no,page in enumerate(pdf.pages,1):
  count=0
  for table in page.extract_tables():
   for row in table:
    if len(row)!=10 or not re.fullmatch(r'\d{4}[A-Z0-9]{3}',clean(row[1])): continue
    assert clean(row[2]) and clean(row[3]), (page_no,row)
    records.append(dict(code=clean(row[1]),institution=clean(row[2]),programme=clean(row[3]),subjectRequirements=[clean(x) for x in row[6:] if clean(x)],historicalCutoffs={'2023':clean(row[4]),'2022':clean(row[5])},page=page_no))
    count+=1
  assert count, f'No programme records on page {page_no}'
  counts.append(count)
assert len({r['code'] for r in records})==len(records),'Duplicate programme codes'
# Conservative programme-family links. Exact university/programme titles remain visible.
patterns={
'c_cs':r'COMPUTER SCIENCE','c_it':r'INFORMATION TECHNOLOGY','c_se':r'SOFTWARE ENGINEERING',
'c_stats':r'STATISTICS|DATA SCIENCE','c_actuarial':r'ACTUARIAL SCIENCE','c_medicine':r'MEDICINE AND (?:BACHELOR OF )?SURGERY|MEDICINE & SURGERY',
'c_nursing':r'NURSING','c_pharmacy':r'BACHELOR OF PHARMACY', 'c_civil':r'CIVIL ENGINEERING',
'c_electrical':r'ELECTRICAL AND ELECTRONIC|ELECTRICAL & ELECTRONIC|ELECTRICAL AND ELECTRONICS',
'c_architecture':r'BACHELOR OF ARCHITECTURE|ARCHITECTURAL STUDIES',
'c_agriculture':r'BACHELOR OF (?:SCIENCE (?:IN )?\()?AGRICULTURE\)?$',
'c_environment':r'ENVIRONMENTAL SCIENCE', 'c_education_sci':r'EDUCATION.*SCIENCE',
'c_education_arts':r'EDUCATION.*ARTS','c_law':r'BACHELOR OF LAWS',
'c_commerce':r'BACHELOR OF COMMERCE','c_economics':r'BACHELOR OF (?:SCIENCE |ARTS )?(?:IN )?\(?ECONOMICS\)?$',
'c_journalism':r'JOURNALISM|MASS COMMUNICATION','c_psychology':r'PSYCHOLOGY',
'c_fine_art':r'FINE ART|FINE ARTS','c_music':r'BACHELOR OF (?:ARTS (?:IN )?\(?)?MUSIC\)?$',
'c_sports_sci':r'SPORTS SCIENCE|SPORT SCIENCE','c_hospitality':r'HOSPITALITY|TOURISM MANAGEMENT'}
asset=json.loads((root/'app/src/main/assets/kcse_university_data.json').read_text())
by_course={c['id']:[r['code'] for r in records if re.search(patterns[c['id']],r['programme']) and not (c['id']=='c_education_sci' and 'ARTS' in r['programme'])] for c in asset['courses']}
for cid,ids in by_course.items():print(cid,len(ids))
data={'meta':{'sourceName':'KUCCPS 2025/2026 Degree Programmes','edition':'2025/2026','fileName':source.name,'sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'pages':len(counts),'recordCount':len(records),'pageCounts':counts,'note':'Programme listings and printed subject requirements are sourced from this edition. 2023/2022 cutoffs are historical, not current eligibility or suitability scores. AI explanations and suitability percentages are guidance, not KUCCPS verification.'},'records':records,'courseNames':{c['id']:c['name'] for c in asset['courses']},'byCourse':by_course}
for folder in ['website','backend','app/src/main/assets']:
 (root/folder/'kuccps_2025.json').write_text(json.dumps(data,ensure_ascii=False,separators=(',',':')),encoding='utf8')
print('TOTAL',len(records),'PAGES',len(counts))
