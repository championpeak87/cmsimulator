var express = require('express');
var router = express.Router();
var filesystem = require('fs');
var fileuploader = require('express-fileupload');

// HTTP STAVOVE KODY
/// USPESNE
const HTTP_OK = 200
const HTTP_CREATED = 201
const HTTP_ACCEPTED = 202
const HTTP_NO_CONTENT = 204

/// CHYBA KLIENTA
const HTTP_BAD_REQUEST = 400
const HTTP_UNAUTHORIZED = 401
const HTTP_FORBIDDEN = 403
const HTTP_NOT_FOUND = 404

const Pool = require('pg').Pool
const pool = new Pool({
  user: 'postgres',
  host: 'localhost',
  database: 'cmsimulator',
  password: '3656a5ec8d234e9cbab536bdd251b8cb',
  port: 5432
})

router.post('/upload', (req, res, next) => {
    const file = req.files.task;
    const file_name = req.query.file_name;
  
    filesystem.mkdir("./uploads/", { recursive: true }, (err) => {
      if (err) { throw err };
    });
    filesystem.mkdir("./uploads/grammarTasks/", { recursive: true }, (err) => {
      if (err) { throw err };
    });
    if (!file) {
      console.log(req);
      console.log("FILE NOT SAVED!");
    }
    else {
      file.mv("./uploads/grammarTasks/" + file_name + ".cmsg", function (err, results) {
        if (err) throw err;
        res.send({
          success: true,
          message: "File uploaded!"
        });
        console.log("NEW TASK UPLOADED!");
      });
    }
  
  });
  
  router.get('/add', (req, res) => {
    const task_name = req.query.task_name;
    const task_description = req.query.task_description;
    const time = req.query.time;
    const assigner = req.query.assigner;
    const public_input = req.query.public_input;
  
  
    pool.query('INSERT INTO grammar_tasks(assigner_id, public_input, task_description, task_name, time) VALUES ($1, $2, $3, $4, $5);',
      [assigner, public_input, task_description, task_name, time], (err, results) => {
        if (err) { throw err; }
  
        if (results.rowCount > 0) {
          pool.query('SELECT * FROM grammar_tasks WHERE assigner_id = $1 AND task_name = $2 AND task_description = $3;', [assigner, task_name, task_description], (error, result) => {
            res.status(HTTP_OK).send(
              {
                task_id: result.rows[0].task_id,
                task_name: task_name,
                task_description: task_description,
                time: time,
                public_input: public_input
              }
            )
          })
        }
      });
  
  })
  
  router.get('/getTasks', (req, res) => {
    const user_id = req.query.user_id;
    const auth_key = '\\x' + req.query.auth_key;
  
    pool.query('select gt.*, gtr.task_status, gt.time - gtr.time_elapsed as remaining_time, gtr.submitted, gtr.submission_date from grammar_tasks as gt left join (SELECT * from grammar_task_results where user_id=$1) as gtr on gtr.task_id = gt.task_id;', [user_id], (err, results) => {
  
      if (err) { throw err }
      if (results.rowCount > 0) {
        res.status(HTTP_OK).send(results.rows);
        console.log(Date(), [user_id], 'has fetched tasks!');
      }
      else {
        res.status(HTTP_OK).send({
          not_found: true
        });
      }
    })
  })
  
  router.get('/delete', (req, res) => {
    const task_id = req.query.task_id;
  
    pool.query('SELECT * FROM grammar_tasks WHERE task_id = $1;', [task_id], (err, result) => {
      if (err) { throw err }
      if (result.rowCount > 0) {
        const filename = "./uploads/grammarTasks/" + result.rows[0].task_id + ".cmsg";
        filesystem.exists(filename, (exists) => {
          if (exists) {
            filesystem.unlink(filename, (error) => {
              if (error) {
                throw error;
              }
            });
          }
          else {
            console.log("TASK COULD NOT BE DELETED! FILE DOES NOT EXIST!");
          }
        });
      }
    })
    pool.query('DELETE FROM grammar_task_results WHERE task_id = $1;', [task_id], (err, result) => {
      if (err) { throw err }
      if (result.rowCount > 0) {
        pool.query('DELETE FROM grammar_tasks WHERE task_id = $1;', [task_id], (error, result2) => {
          if (error) { throw error }
          console.log([task_id], "Task has been deleted!");
          res.status(HTTP_OK).send({
            task_id: task_id,
            deleted: true
          });
        })
      }
      else {
        pool.query('DELETE FROM grammar_tasks WHERE task_id = $1;', [task_id], (error, result2) => {
          if (error) { throw error }
          console.log([task_id], "Task has been deleted!");
          res.status(HTTP_OK).send({
            task_id: task_id,
            deleted: true
          });
        })
      }
    })
  })
  
  router.get('/changeFlag', (req, res) => {
    const task_id = req.query.task_id;
    const user_id = req.query.user_id;
    const task_status = req.query.task_status;
  
    pool.query('SELECT * FROM grammar_task_results WHERE task_id = $1 AND user_id = $2;', [task_id, user_id], (err, result) => {
      if (err) { throw err; }
      if (result.rowCount > 0) {
        if (task_status == "too_late") {
          pool.query('SELECT time FROM grammar_tasks where task_id = $1;', [task_id], (er, rs) => {
            if (er) { throw er }
            if (rs.rowCount > 0) {
              const available_time = rs.rows[0].time;
              var interval = "";
              if (available_time.hours) {
                interval += available_time.hours + ":";
              }
              else {
                interval += "00:";
              }
  
              if (available_time.minutes) {
                interval += available_time.minutes + ":";
              }
              else {
                interval += "00:";
              }
  
              if (available_time.seconds) {
                interval += available_time.seconds;
              }
              else {
                interval += "00";
              }
  
              pool.query('UPDATE grammar_task_results SET task_status=\'too_late\', time_elapsed = $1 WHERE task_id = $2 AND user_id = $3;', [interval, task_id, user_id], (e, r) => {
                if (e) { throw e }
                if (r.rowCount > 0) {
                  res.status(HTTP_OK).send({
                    task_id: task_id,
                    user_id: user_id,
                    task_status: task_status,
                    updated: true
                  });
                }
                else {
                  res.status(HTTP_OK).send({
                    task_id: task_id,
                    user_id: user_id,
                    task_status: task_status,
                    updated: true
                  });
                }
              })
            }
          });
        }
        else {
          pool.query('UPDATE grammar_task_results SET task_status=$1 WHERE task_id = $2 AND user_id = $3;', [task_status, task_id, user_id], (error, result2) => {
            if (error) { throw error }
            res.status(HTTP_OK).send(
              {
                task_id: task_id,
                user_id: user_id,
                task_status: task_status,
                updated: true
              }
            );
          });
        }
      }
      else {
        filesystem.mkdir("./uploads/" + user_id + "/", { recursive: true }, (err) => {
          if (err) { throw err };
        });
        filesystem.copyFile("./uploads/grammarTasks/" + task_id + ".cmsg", "./uploads/" + user_id + "/" + task_id + ".cmsg", (error) => {
          if (error) { throw error; }
        }
        );
        pool.query("INSERT INTO grammar_task_results(task_id, time_elapsed, task_status, user_id) VALUES ($1, $2, $3, $4);", [task_id, '00:00:00', task_status, user_id], (error, result2) => {
          if (error) { throw error }
          res.status(HTTP_OK).send(
            {
              task_id: task_id,
              user_id: user_id,
              task_status: task_status,
              updated: true
            }
          )
        })
      }
    })
  })
  
  router.get('/download', (req, res) => {
    const task_id = req.query.task_id;
    const user_id = req.query.user_id;
  
    if (!user_id) {
      pool.query('SELECT * FROM grammar_tasks WHERE task_id = $1;', [task_id], (err, result) => {
        if (err) { throw err }
        {
          if (result.rowCount > 0) {
            const filePath = "./uploads/grammarTasks/" + task_id + ".cmsg";
            res.status(HTTP_OK).download(filePath);
          }
          else {
            res.status(HTTP_NOT_FOUND).send(
              {
                task_id: task_id,
                found: false
              }
            );
          }
        }
      })
    }
    else {
      pool.query('SELECT * FROM grammar_task_results WHERE user_id = $1 AND task_id = $2;', [user_id, task_id], (error, results) => {
        if (error) { throw error }
        if (results.rowCount > 0) {
          pool.query('SELECT task_id FROM grammar_tasks WHERE task_id = $1;', [task_id], (err, result) => {
            if (err) { throw err }
            {
              if (result.rowCount > 0) {
                const filePath = "./uploads/" + user_id + "/" + task_id + ".cmsg";
                res.status(HTTP_OK).download(filePath);
              }
              else {
                res.status(HTTP_NOT_FOUND).send(
                  {
                    task_id: task_id,
                    found: false
                  }
                );
              }
            }
          })
        }
        else {
          pool.query('SELECT task_id FROM grammar_tasks WHERE task_id = $1;', [task_id], (err, result) => {
            if (err) { throw err }
            {
              if (result.rowCount > 0) {
                const filePath = "./uploads/grammarTasks/" + task_id + ".cmsg";
                res.status(HTTP_OK).download(filePath);
              }
              else {
                res.status(HTTP_NOT_FOUND).send(
                  {
                    task_id: task_id,
                    found: false
                  }
                );
              }
            }
          })
        }
      })
    }
  })
  
  router.get('/updateTimer', (req, res) => {
    const user_id = req.query.user_id;
    const task_id = req.query.task_id;
    const time_elapsed = req.query.time_elapsed;
  
    pool.query('SELECT count(*) from grammar_task_results WHERE user_id = $1 AND task_id = $2;', [user_id, task_id], (err, results) => {
      if (err) { throw err }
      if (results.rows[0].count > 0) {
        pool.query('UPDATE grammar_task_results SET time_elapsed=$1 WHERE user_id = $2 AND task_id = $3;', [time_elapsed, user_id, task_id], (error, result) => {
          if (error) { throw error }
          if (result.rowCount > 0) {
            res.status(HTTP_OK).send(
              {
                task_id: task_id,
                user_id: user_id,
                time_elapsed: time_elapsed,
                updated: true
              }
            );
          } else {
            res.status(HTTP_OK).send(
              {
                task_id: task_id,
                user_id: user_id,
                time_elapsed: time_elapsed,
                updated: false
              }
            );
          }
        })
      }
      else {
        res.status(HTTP_OK).send(
          {
            task_id: task_id,
            user_id: user_id,
            time_elapsed: time_elapsed,
            updated: true
          }
        );
      }
    })
  })
  
  router.get('/submit', (req, res) => {
    const task_id = req.query.task_id;
    const user_id = req.query.user_id;
    const task_status = req.query.task_status;
    const submission_time = req.query.submission_time;
  
    pool.query('SELECT * FROM grammar_task_results WHERE user_id = $1 AND task_id = $2 LIMIT 1;', [user_id, task_id], (err, results) => {
      if (err) { throw err }
      if (results.rowCount > 0) {
        pool.query('UPDATE grammar_task_results SET submitted=\'true\', submission_date=$1, task_status = $2 WHERE task_id = $3 AND user_id = $4;', [submission_time, task_status, task_id, user_id], (error, result) => {
          if (error) { throw error }
          if (result.rowCount > 0) {
            console.log("SUBMITTED");
            res.status(HTTP_OK).send({
              task_id: task_id,
              user_id: user_id,
              submitted: true
            });
          }
        });
      }
    })
  })
  
  router.post('/save', (req, res, next) => {
    const user_id = req.query.user_id;
    const file_name = req.query.file_name;
    const file = req.files.task;
    const makedirPath = "./uploads/" + user_id;
    const path = "./uploads/" + user_id + "/" + file_name;
  
    filesystem.mkdir("./uploads/", { recursive: true }, (err) => {
      if (err) { throw err };
    });
    filesystem.mkdir(makedirPath, { recursive: true }, (err) => {
      if (err) { throw err };
    });
    if (!file) {
      console.log("FILE NOT SAVED!");
    }
    else {
      file.mv(path, function (err, results) {
        if (err) throw err;
        res.status(HTTP_OK).send({
          success: true,
          message: "File uploaded!"
        });
        console.log([user_id], "TASK WAS SAVED!");
      });
    }
  });

module.exports = router;
