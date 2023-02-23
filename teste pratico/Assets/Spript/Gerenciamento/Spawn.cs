using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Spawn : MonoBehaviour
{
    [SerializeField]
    private GameObject  _shooter, _chaser;
    [SerializeField]
    private Transform[] _spawnShooter, _spawnChaser;
    [SerializeField]
    private float       _timeSpawm;    
    private int         _NumSpawm;    
    public static int   _limiteSpawm;
    [SerializeField]
    private Shooter     _pShooter;    

    void Start()
    {
        
    }
    
    void Update()
    {
        if (_limiteSpawm <= 7)
        {
            SpawnerShooter();
        }
        if (_NumSpawm == -1)
        {
            _NumSpawm = Random.Range(0, 100);
        }
    }
    void SpawnerShooter()
    {        
        if (_NumSpawm % 2 == 0)
        {
            _NumSpawm = 0;
            _pShooter._speedRotate = 25;
        }
        else if (_NumSpawm % 2 != 0)
        {
            _NumSpawm = 1;
            _pShooter._speedRotate = 22;
        }
        _timeSpawm += 1 * Time.deltaTime;
        if (_timeSpawm > menu._spawnInim)
        {
            GameObject TempShooter = Instantiate(_shooter);
            TempShooter.transform.position = _spawnShooter[_NumSpawm].position;
            GameObject TempChaser = Instantiate(_chaser);
            TempChaser.transform.position = _spawnChaser[_NumSpawm].position;
            _timeSpawm = 0;
            _NumSpawm = -1;
            _limiteSpawm++;
        }
    }
}

